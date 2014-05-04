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

import com.logpie.auth.logic.TokenScopeManager.Scope;
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
            AuthServiceLog.e(TAG, "JSONException when getting requestID");
            AuthServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        String email;
        try
        {
            email = regData.getString(AuthRequestKeys.KEY_REGISTER_EMAIL);
            String password = regData.getString(AuthRequestKeys.KEY_REGISTER_PASSWORD);
            // Generate the new tokens for all new users
            String sql = SQLHelper.buildCreateUserSQL(email, password, buildNewUserScope());
            sAuthDataManager.executeInsert(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        String uid = result.getString(AuthDataManager.KEY_INSERT_RESULT_ID);
                        JSONObject returnData = new JSONObject();
                        returnData.put(AuthResponseKeys.KEY_REQUEST_ID, request_id);
                        returnData.put(AuthResponseKeys.KEY_AUTH_RESULT,
                                AuthResponseKeys.AUTH_RESULT_SUCCESS);
                        returnData.put(AuthResponseKeys.KEY_USER_ID, uid);
                        handleRegistrationSuccess(returnData, response);
                    } catch (JSONException e)
                    {
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
            AuthServiceLog.e(TAG, "JSONException when getting email/password");
            AuthServiceLog.e(TAG, e.getMessage());
        }
    }

    private void handleRegistrationSuccess(JSONObject data, HttpServletResponse response)
    {
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
            sAuthDataManager.executeQueryForLogin(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {

                        result.put(AuthResponseKeys.KEY_REQUEST_ID, request_id);
                        result.put(AuthResponseKeys.KEY_AUTH_RESULT,
                                AuthResponseKeys.AUTH_RESULT_SUCCESS);
                        handleRegistrationSuccess(result, response);
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
            AuthServiceLog.e(TAG, "JSONException when getting email/password");
            AuthServiceLog.e(TAG, e.getMessage());
        }
    }
}
