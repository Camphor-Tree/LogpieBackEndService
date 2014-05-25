package com.logpie.auth.logic;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.auth.data.AuthDataManager;
import com.logpie.auth.data.AuthDataManager.DataCallback;
import com.logpie.auth.data.SQLHelper;
import com.logpie.auth.exception.EmailAlreadyExistException;
import com.logpie.auth.logic.TokenScopeManager.Scope;
import com.logpie.auth.tool.AuthErrorMessage;
import com.logpie.auth.tool.AuthErrorType;
import com.logpie.service.common.exception.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;
import com.logpie.service.common.helper.HttpResponseWriter;
import com.logpie.service.common.helper.TimeHelper;

public class AuthenticationManager
{
    public static enum AuthenticationType
    {
        AUTHENTICATE, REGISTER, RESET_PASSWORD, TOKEN_EXCHANGE, FORGET_PASSWORD, TOKEN_VALIDATION;
        public static AuthenticationType matchType(String type)
        {
            for (AuthenticationType requestType : AuthenticationType.values())
            {
                if (requestType.toString().equals(type))
                    return requestType;
            }
            return null;
        }
    }

    private static String TAG = AuthenticationManager.class.getName();
    private static AuthenticationManager sAuthenticationManager;

    //private static ServletContext sGlobalUniqueContext;
    private static AuthDataManager sAuthDataManager;

    public static void initialize(ServletContext globalUniqueContext)
    {
        //sGlobalUniqueContext = globalUniqueContext;
        sAuthDataManager = AuthDataManager.getInstance();
    }

    public synchronized static AuthenticationManager getInstance()
    {
        if (sAuthenticationManager == null)
        {
            CommonServiceLog.d(TAG, "Should initialize only once!");
            sAuthenticationManager = new AuthenticationManager();
            sAuthDataManager = AuthDataManager.getInstance();
        }
        return sAuthenticationManager;
    }

    private AuthenticationManager()
    {

    }

    /**
     * Central handleAuthentication TODO: Currently we just support all the auth
     * data is stored in http post body. Future, we should also support parse
     * the header information.
     * 
     * @param request
     */
    public void handleAuthenticationRequest(HttpServletRequest request, HttpServletResponse response)
    {
        final long startTime = System.currentTimeMillis();
        JSONObject postBody = null;
		try {
			postBody = HttpRequestParser.httpRequestParser(request);
		} catch (HttpRequestIsNullException e) {
			CommonServiceLog.e(TAG, "The coming request is null!It should be a bug of tomcat!");
			e.printStackTrace();
			return;
		}
        if (postBody != null)
        {
            CommonServiceLog.d(TAG, "[Receiving request data:]" + postBody.toString());
            AuthenticationType type = getAuthenticationType(postBody);
            switch (type)
            {
            case REGISTER:
                handleRegister(postBody, response);
                break;
            case AUTHENTICATE:
            {
                handleAuthenticate(postBody, response);
                long latency = System.currentTimeMillis()- startTime;
                CommonServiceLog.d(TAG, "Authenticate, totally time consuming:" + latency);
                break;
            }
            case RESET_PASSWORD:
                break;
            case TOKEN_EXCHANGE:
                break;
            case FORGET_PASSWORD:
                break;
            case TOKEN_VALIDATION:
                break;
            default:
            {
                CommonServiceLog.e(TAG, "Unsupported Type!");
                handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
                break;
            }

            }
        }
        else
        {
            handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
        }

    };

    private void handleAuthenticationResponseWithError(HttpServletResponse response,
            AuthErrorType errorType)
    {
        try
        {
            response.sendError(errorType.getErrorCode());
            CommonServiceLog.e(
                    TAG,
                    "Returning error code when handling authentication ->" + "ErrorCode:"
                            + errorType.getErrorCode() + " ErrorMessage:"
                            + errorType.getErrorMessage());
        } catch (IOException e)
        {
            CommonServiceLog.e(TAG, "IOException happend when sendErrorCode");
        }
    }

    private AuthenticationType getAuthenticationType(JSONObject postData)
    {
        if (postData.has(AuthRequestKeys.KEY_AUTHENTICATION_TYPE))
        {
            try
            {
                return AuthenticationType.matchType(postData
                        .getString(AuthRequestKeys.KEY_AUTHENTICATION_TYPE));
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException happend when parsing Authentication Type");
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Handle Register
     * 
     * @param regData
     *            sample regData: {"login_email":"yilei@aa.com","request_id":
     *            "36619c29-d767-445b-9a8c-8f20b5e53ad6"
     *            ,"login_password":"123456"}
     * @param response
     */
    private void handleRegister(JSONObject regData, final HttpServletResponse response)
    {
        String requestID = null;
        try
        {
            // If exception happened, just generate a random requestID
            requestID = regData.getString(AuthRequestKeys.KEY_REQUEST_ID) != null ? regData
                    .getString(AuthRequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();
            CommonServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            CommonServiceLog.e(TAG,
                    "JSONException when getting requestID, setting a new random requestID");
            CommonServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        try
        {
        	// get email & password
            final String email = regData.getString(AuthRequestKeys.KEY_REGISTER_EMAIL);
            String password = regData.getString(AuthRequestKeys.KEY_REGISTER_PASSWORD);
            // check whether the email already exist.

            // Generate the new tokens for all new users
            String sql = SQLHelper.buildCreateUserStep1SQL(email, password);
            sAuthDataManager =  AuthDataManager.getInstance();
            sAuthDataManager.executeInsertAndGetUID(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                	if(result==null||!result.has(AuthResponseKeys.KEY_USER_ID))
                	{
                		handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
                		return;
                	}
                	
                	
                    try
                    {
                    	String uid = result.getString (AuthResponseKeys.KEY_USER_ID);
                    	//the result contains sql and two tokens
                    	ArrayList<String> resultArray = SQLHelper.buildCreateUserStep2SQL(uid,TokenScopeManager.buildNewUserScope());
                    	String sql = resultArray.get(0);
                    	boolean success = sAuthDataManager.executeNoResult(sql);
                    	if(success)
                    	{
                    		result.put(AuthResponseKeys.KEY_EMAIL,email);
                    		result.put(AuthResponseKeys.KEY_ACCESS_TOKEN, resultArray.get(1));
                    		result.put(AuthResponseKeys.KEY_REFRESH_TOKEN, resultArray.get(2));
                    		handleAuthResult(true, result, response);
                    	}
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
                        CommonServiceLog.e(TAG, request_id+":JSONException when getting insert result");
                        CommonServiceLog.e(TAG, request_id+":"+e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        handleAuthenticationResponseWithError(response, AuthErrorType.valueOf(error
                                .getString(AuthDataManager.KEY_CALLBACK_ERROR)));
                    } catch (JSONException e)
                    {
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG, "JSONException when getting email/password");
            CommonServiceLog.e(TAG, e.getMessage());
        } catch (EmailAlreadyExistException e)
        {
            CommonServiceLog.e(TAG, "email already exist");
            JSONObject errorMessage = new JSONObject();
            try
            {
                errorMessage.put(AuthResponseKeys.KEY_ERROR_MESSAGE,
                        AuthErrorMessage.ERROR_EMAIL_ALREADY_EXIST);
                handleAuthResult(false, errorMessage, response);
            } catch (JSONException e1)
            {
                handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
            }

        }
        
       
    }

    private void handleAuthResult(boolean success, JSONObject data, HttpServletResponse response)
            throws JSONException
    {
        CommonServiceLog.d(TAG, data.toString());
        if (success)
        {
            data.put(AuthResponseKeys.KEY_AUTH_RESULT, AuthResponseKeys.AUTH_RESULT_SUCCESS);
        }
        else
        {
            data.put(AuthResponseKeys.KEY_AUTH_RESULT, AuthResponseKeys.AUTH_RESULT_ERROR);
        }
        HttpResponseWriter.reponseWithSuccess(AuthResponseKeys.KEY_AUTH_RESULT,data, response);
    }



    /**
     * Handle Register
     * 
     * @param regData
     * @param response
     */
    private void handleAuthenticate(JSONObject loginData, final HttpServletResponse response)
    {
        String requestID = null;
        try
        {
            // If exception happened, just generate a random requestID
            requestID = loginData.getString(AuthRequestKeys.KEY_REQUEST_ID) != null ? loginData
                    .getString(AuthRequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();
            CommonServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
            CommonServiceLog.d(TAG, "Received registration data:" + loginData);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            CommonServiceLog.e(TAG, "JSONException when getting requestID");
            CommonServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        String email;
        try
        {   //Step1: Verify Email&Password
            email = loginData.getString(AuthRequestKeys.KEY_LOGIN_EMAIL);
            String password = loginData.getString(AuthRequestKeys.KEY_LOGIN_PASSWORD);
            if(email==null||password==null)
            {
            	handleAuthenticationResponseWithError(response, AuthErrorType.AUTH_ERROR);
            	return;
            }
            
            
            // Generate the new tokens for all new users
            String sql = SQLHelper.buildLoginSQL(email, password);
            //Step1: Verify the email and password
            JSONObject step1_result = sAuthDataManager.executeQueryForLoginStep1(sql);
            if (step1_result == null)
            {
                handleAuthenticationResponseWithError(response, AuthErrorType.AUTH_ERROR);
                return;
            }
            
            //Step2: Check the expiration time of accessToken & refreshToken
            //We do not refresh anyway, because the user may login to multiple devices, if refresh anyway, 
            //it will invalid the tokens in previous devices, which is a bad user experience
            final String uid = step1_result.getString(AuthResponseKeys.KEY_USER_ID);
            String access_token =  step1_result.getString(AuthResponseKeys.KEY_ACCESS_TOKEN);
            String refresh_token =  step1_result.getString(AuthResponseKeys.KEY_REFRESH_TOKEN);
            Timestamp access_token_expiration = Timestamp.valueOf(step1_result.getString(AuthResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION));
            Timestamp refresh_token_expiration = Timestamp.valueOf(step1_result.getString(AuthResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION));
            String currentTime = TimeHelper.getCurrentTimestamp().toString();
            CommonServiceLog.d(TAG,"currentTime:"+currentTime);
        	CommonServiceLog.d(TAG,"access_token_expiration:"+access_token_expiration.toString());
        	CommonServiceLog.d(TAG,"refresh_token_expiration:"+refresh_token_expiration.toString());
            if(access_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            { 
            	CommonServiceLog.d(TAG, "access_token already expire, refresh the access_token");
	        	//(Step3.1): If the access_token already expire, update the token for the user.
	            String sql_step2 = SQLHelper.buildUpdateAccessTokenSQL(uid, access_token);
	            boolean step2_result = sAuthDataManager.executeQueryForLoginStep2(sql_step2);
	            if (!step2_result)
	            {
	                CommonServiceLog.e(sql,
	                        "Error happend in authentication Step2, refresh the access_token");
	                CommonServiceLog.logRequest(TAG, requestID,
	                        "Error happend in authentication Step2, refresh the access_token");
	                handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
	                return;
	            }
            }
            else
            {
            	CommonServiceLog.d(TAG, "access_token still valid, won't refresh to disturb the account in other device");
            }
            if(refresh_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            { 
            	CommonServiceLog.d(TAG, "refresh_token already expire, refresh the refresh_token");
	        	//(Step3.2): If the refresh_token already expire, update the token for the user.
	            String sql_step2 = SQLHelper.buildUpdateRefreshTokenSQL(uid, refresh_token);
	            boolean step2_result = sAuthDataManager.executeQueryForLoginStep2(sql_step2);
	            if (!step2_result)
	            {
	                CommonServiceLog.e(sql,"Error happend in authentication Step3, refresh the refresh_token");
	                CommonServiceLog.logRequest(TAG, requestID,
	                        "Error happend in authentication Step3, refresh the refresh_token");
	                handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
	                return;
	            }
            }
            else
            {
            	CommonServiceLog.d(TAG, "refresh_token still valid, won't refresh to disturb the account in other device");
            }
            	
            //Step4. Do a query to again to ensure the token is valid from database
            sAuthDataManager.executeQueryForLoginStep4(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {

                    try
                    {
                        result.put(AuthResponseKeys.KEY_REQUEST_ID, request_id);
                        handleAuthResult(true, result, response);
                    } catch (JSONException e)
                    {
                        handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
                        CommonServiceLog.e(TAG, "JSONException when getting insert result");
                        CommonServiceLog.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            CommonServiceLog.e(TAG, "JSONException when getting email/password");
            CommonServiceLog.e(TAG, e.getMessage());
        }
    }

}
