package com.logpie.auth.logic;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.auth.exception.EmailAlreadyExistException;
import com.logpie.auth.logic.TokenScopeManager.Scope;
import com.logpie.auth.tool.AuthErrorMessage;
import com.logpie.auth.tool.AuthErrorType;
import com.logpie.auth.tool.AuthServiceLog;
import com.logpie.auth.tool.HttpRequestParser;
import com.logpie.auth.tool.HttpResponseWriter;
import comg.logpie.auth.data.AuthDataManager;
import comg.logpie.auth.data.AuthDataManager.DataCallback;
import comg.logpie.auth.data.SQLHelper;

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

    private static ServletContext sGlobalUniqueContext;
    private static AuthDataManager sAuthDataManager;

    public static void initialize(ServletContext globalUniqueContext)
    {
        sGlobalUniqueContext = globalUniqueContext;
        sAuthDataManager = AuthDataManager.getInstance();
    }

    public synchronized static AuthenticationManager getInstance()
    {
        if (sAuthenticationManager == null)
        {
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
        JSONObject postBody = HttpRequestParser.httpRequestParser(request);
        if (postBody != null)
        {
            AuthenticationType type = getAuthenticationType(postBody);
            switch (type)
            {
            case REGISTER:
                handleRegister(postBody, response);
                break;
            case AUTHENTICATE:
                handleAuthenticate(postBody, response);
                break;
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
                AuthServiceLog.e(TAG, "Un Supported Type!");
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
            AuthServiceLog.e(
                    TAG,
                    "Returning error code when handling authentication ->" + "ErrorCode:"
                            + errorType.getErrorCode() + " ErrorMessage:"
                            + errorType.getErrorMessage());
        } catch (IOException e)
        {
            AuthServiceLog.e(TAG, "IOException happend when sendErrorCode");
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
                AuthServiceLog.e(TAG, "JSONException happend when parsing Authentication Type");
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
            AuthServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
            AuthServiceLog.d(TAG, "Received registration data:" + regData);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            AuthServiceLog.e(TAG,
                    "JSONException when getting requestID, setting a new random requestID");
            AuthServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        try
        {
            String email = regData.getString(AuthRequestKeys.KEY_REGISTER_EMAIL);
            String password = regData.getString(AuthRequestKeys.KEY_REGISTER_PASSWORD);
            // check whether the email already exist.

            // Generate the new tokens for all new users
            String sql = SQLHelper.buildCreateUserSQL(email, password, buildNewUserScope());
            sAuthDataManager.executeInsert(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        String uid = String.valueOf(result
                                .getInt(AuthDataManager.KEY_INSERT_RESULT_ID));
                        JSONObject returnData = new JSONObject();
                        returnData.put(AuthResponseKeys.KEY_REQUEST_ID, request_id);
                        returnData.put(AuthResponseKeys.KEY_USER_ID, uid);
                        handleAuthResult(true, returnData, response);
                    } catch (JSONException e)
                    {
                        AuthServiceLog.logRequest(TAG, request_id, e.getMessage());
                        AuthServiceLog.e(TAG, "JSONException when getting insert result");
                        AuthServiceLog.e(TAG, e.getMessage());
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
            AuthServiceLog.logRequest(TAG, request_id, e.getMessage());
            handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
            AuthServiceLog.e(TAG, "JSONException when getting email/password");
            AuthServiceLog.e(TAG, e.getMessage());
        } catch (EmailAlreadyExistException e)
        {
            AuthServiceLog.e(TAG, "email already exist");
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
        if (success)
        {
            data.put(AuthResponseKeys.KEY_AUTH_RESULT, AuthResponseKeys.AUTH_RESULT_SUCCESS);
        }
        else
        {
            data.put(AuthResponseKeys.KEY_AUTH_RESULT, AuthResponseKeys.AUTH_RESULT_ERROR);
        }
        HttpResponseWriter.reponseWithSuccess(data, response);
    }

    /**
     * Build the scopes for new user. The token would contain the information
     * about whether the user can access specific logpie service
     * 
     * @return list of the scopes, new user can access to
     */
    private List<Scope> buildNewUserScope()
    {
        List<Scope> scopes = new LinkedList<Scope>();
        scopes.add(Scope.RocketService);
        scopes.add(Scope.ActivityService);
        scopes.add(Scope.UserService);
        scopes.add(Scope.AuthenticationService);
        return scopes;
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
            AuthServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
            AuthServiceLog.d(TAG, "Received registration data:" + loginData);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            AuthServiceLog.e(TAG, "JSONException when getting requestID");
            AuthServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        String email;
        try
        {
            email = loginData.getString(AuthRequestKeys.KEY_LOGIN_EMAIL);
            String password = loginData.getString(AuthRequestKeys.KEY_LOGIN_PASSWORD);
            // Generate the new tokens for all new users
            String sql = SQLHelper.buildLoginSQL(email, password);

            JSONObject step1_result = sAuthDataManager.executeQueryForLoginStep1(sql);
            if (step1_result == null)
            {
                handleAuthenticationResponseWithError(response, AuthErrorType.AUTH_ERROR);
                return;
            }
            else
            {
                String sql_step2 = SQLHelper.buildUpdateTokenSQL(
                        step1_result.getString(AuthResponseKeys.KEY_USER_ID), email, password,
                        step1_result.getString(AuthResponseKeys.KEY_ACCESS_TOKEN));
                boolean step2_result = sAuthDataManager.executeQueryForLoginStep2(sql_step2);
                if (!step2_result)
                {
                    AuthServiceLog.e(sql,
                            "Error happend in authentication Step2, refresh all the token");
                    AuthServiceLog.logRequest(TAG, requestID,
                            "Error happend in authentication Step2, refresh all the token");
                    handleAuthenticationResponseWithError(response, AuthErrorType.SEVER_ERROR);
                    return;
                }
            }

            // TODO:Verify and improve this flow. Currently if step2 fails, we
            // will stop the authentication, because the token didn't get
            // refresh, it's meaningless to return the old-token.
            // But in some edge cases, such as user change a device to login, it
            // can return the token, because the token is still valid.
            // There are two flows,
            // 1. user's refresh_token invalid, force to login again. (should
            // fails in step 2)
            // 2. user change a device, login normally (shouldn't fails in
            // step2, the token maybe still valid)
            sAuthDataManager.executeQueryForLoginStep3(sql, new DataCallback()
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
                        AuthServiceLog.e(TAG, "JSONException when getting insert result");
                        AuthServiceLog.e(TAG, e.getMessage());
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
            AuthServiceLog.logRequest(TAG, request_id, e.getMessage());
            AuthServiceLog.e(TAG, "JSONException when getting email/password");
            AuthServiceLog.e(TAG, e.getMessage());
        }
    }

}
