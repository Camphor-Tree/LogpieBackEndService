package com.logpie.auth.logic;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.logpie.commonlib.RequestKeys;
import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.authentication.AuthenticationError;
import com.logpie.service.error.ErrorMessage;
import com.logpie.service.error.ErrorType;
import com.logpie.service.error.HttpRequestIsNullException;
import com.logpie.service.util.HttpRequestParser;
import com.logpie.service.util.HttpResponseWriter;
import com.logpie.service.util.LatencyHelper;
import com.logpie.service.util.ServiceLog;
import com.logpie.service.util.TimeHelper;

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
                {
                    ServiceLog.d("Enum", type);
                    return requestType;
                }
            }
            return null;
        }
    }

    private static String TAG = AuthenticationManager.class.getName();
    private static AuthenticationManager sAuthenticationManager;

    // private static ServletContext sGlobalUniqueContext;
    private static AuthDataManager sAuthDataManager;

    // The map to store email under registration to ensure the register is
    // thread safe when checking the email is available or not
    private static HashSet<String> sEmailsUnderRegistration;

    public static void initialize(ServletContext globalUniqueContext)
    {
        // sGlobalUniqueContext = globalUniqueContext;
        sAuthDataManager = AuthDataManager.getInstance();
        sEmailsUnderRegistration = new HashSet<String>();
    }

    public synchronized static AuthenticationManager getInstance()
    {
        if (sAuthenticationManager == null)
        {
            ServiceLog.d(TAG, "Should initialize only once!");
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
        JSONObject postBody = null;
        try
        {
            postBody = HttpRequestParser.httpRequestParser(request);
        } catch (HttpRequestIsNullException e)
        {
            ServiceLog.e(TAG, "The coming request is null!It should be a bug of tomcat!", e);
            return;
        }
        if (postBody != null)
        {
            ServiceLog.d(TAG, "[Receiving request data:]" + postBody.toString());
            AuthenticationType type = getAuthenticationType(postBody);
            if (type == null)
            {
                ServiceLog.d(TAG, "authentication type is null!");
                return;
            }
            final LatencyHelper latencyHelper = new LatencyHelper(type.name());
            switch (type)
            {
            case REGISTER:
            {
                handleRegister(postBody, response);
                break;
            }
            case AUTHENTICATE:
            {
                handleAuthenticate(postBody, response);
                break;
            }
            case RESET_PASSWORD:
            {
                handleResetPassword(postBody, response);
                break;
            }
            case TOKEN_EXCHANGE:
            {
                handleTokenExchange(postBody, response);
                break;
            }
            case FORGET_PASSWORD:
                break;
            case TOKEN_VALIDATION:
            {
                handleTokenValidation(postBody, response);
                break;
            }
            default:
            {
                ServiceLog.e(TAG, "Unsupported Type!");
                handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, null);
                break;
            }

            }
            latencyHelper.stopAndGetLantency();
            latencyHelper.logLatencyInformation(TAG);
        }
        else
        {
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, null);
        }

    };

    private void handleAuthenticationResponseWithError(HttpServletResponse response,
            ErrorType errorType, String requestId)
    {
        try
        {
            response.sendError(errorType.getErrorCode());
            ServiceLog.e(TAG, "Returning error code when handling authentication ->" + "ErrorCode:"
                    + errorType.getErrorCode() + " ErrorMessage:" + errorType.getErrorMessage(),
                    requestId);
        } catch (IOException e)
        {
            ServiceLog.e(TAG, "IOException happend when sendErrorCode", e);
        }
    }

    private AuthenticationType getAuthenticationType(JSONObject postData)
    {
        if (postData.has(RequestKeys.KEY_REQUEST_TYPE))
        {
            try
            {
                return AuthenticationType.matchType(postData
                        .getString(RequestKeys.KEY_REQUEST_TYPE));
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException happend when parsing Authentication Type", e);
                return null;
            }
        }
        else
        {
            ServiceLog.e(TAG, "The request doesn't contain any Request_Type");
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
        final String request_id = getAndLogRequestId(regData);
        try
        {
            // get email & password
            final String email = regData.getString(RequestKeys.KEY_EMAIL);
            String password = regData.getString(RequestKeys.KEY_PASSWORD);
            String nickName = regData.getString(RequestKeys.KEY_NICKNAME);
            final String city = regData.getString(RequestKeys.KEY_CITY);
            if (nickName == null || nickName.equals(""))
            {
                nickName = "New User";
            }
            final String userNickName = nickName;

            // Step 0: check the user is existd or not
            String sql = SQLHelper.buildCheckUserDuplicateSQL(email);
            sAuthDataManager = AuthDataManager.getInstance();
            boolean isUserExisted = sAuthDataManager.executeQueryForCheckDuplicate(sql);
            if (isUserExisted && sEmailsUnderRegistration.contains(email))
            {
                ServiceLog.e(TAG, "email already exist", request_id);
                JSONObject errorMessage = new JSONObject();
                try
                {
                    errorMessage.put(ResponseKeys.KEY_ERROR_MESSAGE,
                            ErrorMessage.ERROR_EMAIL_ALREADY_EXIST);
                    handleAuthResult(false, errorMessage, response, request_id);
                } catch (JSONException e1)
                {
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                }
            }
            else
            {
                sEmailsUnderRegistration.add(email);
                // Step 1: just insert into the database and get the uid back
                sql = SQLHelper.buildCreateUserStep1SQL(email, password);
                sAuthDataManager.executeInsertAndGetUIDandEmail(sql, new DataCallback()
                {
                    @Override
                    public void onSuccess(JSONObject result)
                    {
                        sEmailsUnderRegistration.remove(email);
                        if (result == null || !result.has(ResponseKeys.KEY_UID))
                        {
                            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                                    request_id);
                            return;
                        }
                        try
                        {
                            String uid = result.getString(ResponseKeys.KEY_UID);
                            String email = result.getString(ResponseKeys.KEY_EMAIL);
                            ServiceLog.d(TAG, "Generating the new uid:" + uid, request_id);
                            // the result contains sql and two tokens
                            ArrayList<String> resultArray = SQLHelper.buildCreateUserStep2SQL(uid,
                                    TokenScopeManager.buildNewUserScope());
                            String sql = resultArray.get(0);
                            boolean success = sAuthDataManager.executeNoResult(sql);
                            if (success)
                            {
                                // TODO: add check to whether it succeed. if not
                                // need to roolback the database.
                                RegisterHelper.callCustomerServiceToRegister(uid, email,
                                        userNickName, city, request_id);
                                ServiceLog.d(TAG, "Update token successfully", request_id);
                                result.put(ResponseKeys.KEY_ACCESS_TOKEN, resultArray.get(1));
                                result.put(ResponseKeys.KEY_REFRESH_TOKEN, resultArray.get(2));
                                handleAuthResult(true, result, response, request_id);
                            }
                            else
                            {
                                ServiceLog.d(TAG, "Update token fail", request_id);
                                handleAuthenticationResponseWithError(response,
                                        ErrorType.SEVER_ERROR, request_id);
                                return;
                            }
                        } catch (JSONException e)
                        {
                            ServiceLog.logRequest(TAG, request_id, e.getMessage());
                            ServiceLog.e(TAG, ":JSONException when getting insert result",
                                    request_id, e);
                            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                                    request_id);
                            return;
                        }

                    }

                    @Override
                    public void onError(JSONObject error)
                    {
                        sEmailsUnderRegistration.remove(email);
                        try
                        {
                            handleAuthenticationResponseWithError(response, ErrorType.valueOf(error
                                    .getString(AuthDataManager.KEY_CALLBACK_ERROR)), request_id);
                        } catch (JSONException e)
                        {
                        }

                    }
                });
            }
        } catch (JSONException e)
        {
            ServiceLog.logRequest(TAG, request_id, e.getMessage());
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
            ServiceLog.e(TAG, "JSONException when getting email/password", request_id, e);

        } catch (EmailAlreadyExistException e)
        {
            ServiceLog.e(TAG, "email already exist", e);
            JSONObject errorMessage = new JSONObject();
            try
            {
                errorMessage.put(ResponseKeys.KEY_ERROR_MESSAGE,
                        ErrorMessage.ERROR_EMAIL_ALREADY_EXIST);
                handleAuthResult(false, errorMessage, response, request_id);
            } catch (JSONException e1)
            {
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }

        }

    }

    private void handleAuthFailWithErrorMessage(final HttpServletResponse response,
            final String error_message, final String request_id) throws JSONException
    {

        JSONObject data = new JSONObject();

        ServiceLog.d(TAG, "Sending auth result back: " + error_message, request_id);

        data.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, ResponseKeys.RESULT_ERROR);

        data.put(ResponseKeys.KEY_ERROR_MESSAGE, error_message);

        HttpResponseWriter.reponseWithSuccess(ResponseKeys.KEY_AUTHENTICATION_RESULT, data,
                response);
    }

    private void handleAuthResult(final boolean success, JSONObject data,
            final HttpServletResponse response, final String request_id) throws JSONException
    {
        if (data == null)
        {
            data = new JSONObject();
        }
        ServiceLog.d(TAG, "Sending auth result back: " + data.toString(), request_id);
        if (success)
        {
            data.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, ResponseKeys.RESULT_SUCCESS);
        }
        else
        {
            data.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, ResponseKeys.RESULT_ERROR);
        }
        HttpResponseWriter.reponseWithSuccess(ResponseKeys.KEY_AUTHENTICATION_RESULT, data,
                response);
    }

    /**
     * Handle authenticate/login flow.
     * 
     * @param regData
     * @param response
     */
    private void handleAuthenticate(JSONObject loginData, final HttpServletResponse response)
    {
        final String request_id = getAndLogRequestId(loginData);

        String email;
        try
        { // Step 1: Verify Email & Password
            email = loginData.getString(RequestKeys.KEY_EMAIL);
            String password = loginData.getString(RequestKeys.KEY_PASSWORD);
            if (email == null || password == null)
            {
                handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
                return;
            }

            // Generate the new tokens for all new users
            String sql = SQLHelper.buildLoginSQL(email, password);
            ServiceLog.d(TAG, "The query email-password sql is:" + sql);
            // Step 1: Verify the email and password
            JSONObject step1_result = sAuthDataManager.executeQueryForLoginStep1(sql);
            if (step1_result == null)
            {
                handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
                return;
            }

            // Step 2: Check the expiration time of accessToken & refreshToken
            // We do not refresh anyway, because the user may login to multiple
            // devices, if refresh anyway,
            // it will invalid the tokens in previous devices, which is a bad
            // user experience
            final String uid = step1_result.getString(ResponseKeys.KEY_UID);
            String access_token = step1_result.getString(ResponseKeys.KEY_ACCESS_TOKEN);
            String refresh_token = step1_result.getString(ResponseKeys.KEY_REFRESH_TOKEN);
            Timestamp access_token_expiration = Timestamp.valueOf(step1_result
                    .getString(ResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION));
            Timestamp refresh_token_expiration = Timestamp.valueOf(step1_result
                    .getString(ResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION));
            String currentTime = TimeHelper.getCurrentTimestamp().toString();
            ServiceLog.d(TAG, "currentTime:" + currentTime);
            ServiceLog.d(TAG, "access_token_expiration:" + access_token_expiration.toString());
            ServiceLog.d(TAG, "refresh_token_expiration:" + refresh_token_expiration.toString());
            if (!access_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            {
                ServiceLog.d(TAG, "access_token already expire, refresh the access_token");
                // (Step 3.1): If the access_token already expire, update the
                // token for the user.
                ArrayList<String> sqlResultWithAccessToken = (ArrayList<String>) SQLHelper
                        .buildUpdateAccessTokenSQL(uid, access_token);
                String sql_step3_1 = sqlResultWithAccessToken.get(0);
                access_token = sqlResultWithAccessToken.get(1);
                boolean step3_1_result = sAuthDataManager.executeUpdateForLoginStep2(sql_step3_1);
                if (!step3_1_result)
                {
                    ServiceLog.e(sql,
                            "Error happend in authentication Step2, refresh the access_token",
                            request_id);
                    ServiceLog.logRequest(TAG, request_id,
                            "Error happend in authentication Step2, refresh the access_token");
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                    return;
                }
            }
            else
            {
                ServiceLog
                        .d(TAG,
                                "access_token still valid, won't refresh to disturb the account in other device",
                                request_id);
            }
            if (!refresh_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            {
                ServiceLog.d(TAG, "refresh_token already expire, refresh the refresh_token",
                        request_id);
                // (Step 3.2): If the refresh_token already expire, update the
                // token for the user.
                ArrayList<String> sqlResultWithRefreshToken = (ArrayList<String>) SQLHelper
                        .buildUpdateRefreshTokenSQL(uid, access_token);
                String sql_step3_2 = sqlResultWithRefreshToken.get(0);
                refresh_token = sqlResultWithRefreshToken.get(1);
                boolean step3_2_result = sAuthDataManager.executeUpdateForLoginStep2(sql_step3_2);
                if (!step3_2_result)
                {
                    ServiceLog.e(sql,
                            "Error happend in authentication Step3, refresh the refresh_token",
                            request_id);
                    ServiceLog.logRequest(TAG, request_id,
                            "Error happend in authentication Step3, refresh the refresh_token");
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                    return;
                }
            }
            else
            {
                ServiceLog
                        .d(TAG,
                                "refresh_token still valid, won't refresh to disturb the account in other device");
            }

            // Step 4. If Step1 to Step3 success, then just add
            // access_token,refresh_token,uid into the response.
            // TODO: If we support checkbox "remember this device", then we
            // should only return access_token when uncheck.
            try
            {
                JSONObject authResult = new JSONObject();
                authResult.put(ResponseKeys.KEY_UID, uid);
                authResult.put(ResponseKeys.KEY_ACCESS_TOKEN, access_token);
                authResult.put(ResponseKeys.KEY_REFRESH_TOKEN, refresh_token);
                authResult.put(ResponseKeys.KEY_RESPONSE_ID, request_id);
                handleAuthResult(true, authResult, response, request_id);
            } catch (JSONException e)
            {
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
                ServiceLog
                        .e(TAG, "Authenticate Step4, JSONException when getting insert result", e);
            }

        } catch (JSONException e)
        {
            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            ServiceLog.logRequest(TAG, request_id, e.getMessage());
            ServiceLog.e(TAG, "JSONException when getting email/password", request_id, e);
        }
    }

    private void handleResetPassword(JSONObject resetPasswordData,
            final HttpServletResponse response)
    {
        final String request_id = getAndLogRequestId(resetPasswordData);

        // Get info from request
        String email = null;
        String oldPassword = null;
        String newPassword = null;
        try
        {
            email = resetPasswordData.getString(RequestKeys.KEY_EMAIL);
            oldPassword = resetPasswordData.getString(RequestKeys.KEY_PASSWORD);
            newPassword = resetPasswordData.getString(RequestKeys.KEY_NEW_PASSWORD);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when try to get reset password infomation.",
                    request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }

        if (email == null || oldPassword == null || newPassword == null)
        {
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
            return;
        }

        // Generate the new tokens for all new users
        String sql = SQLHelper.buildLoginSQL(email, oldPassword);
        ServiceLog.d(TAG, "Reset password SQL is: " + sql);
        // Step1: Verify the email and old password
        JSONObject step1_result = sAuthDataManager.executeQueryForLoginStep1(sql);
        if (step1_result == null)
        {
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
            return;
        }

        // Step2: Update the new password
        // get uid
        String uid = null;
        try
        {
            uid = step1_result.getString(ResponseKeys.KEY_UID);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when try to get uid.", request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
        }
        if (uid == null)
        {
            ServiceLog.e(TAG, "uid must not be null!", request_id);
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
        }
        String resetPasswordSQL = SQLHelper.buildResetPasswordSQL(uid, newPassword);
        ServiceLog.d(TAG, "Reset password SQL is: " + resetPasswordSQL);
        boolean resetSuccess = sAuthDataManager.executeUpdate(resetPasswordSQL);
        if (resetSuccess)
        {
            try
            {
                // reset password just sendback the success signal
                handleAuthResult(true, null, response, request_id);
            } catch (JSONException e)
            {
                // retry once.
                // reset password just sendback the success signal
                try
                {
                    handleAuthResult(true, null, response, request_id);
                } catch (JSONException e1)
                {
                    ServiceLog.e(TAG, "JSONException when try to put success signal", request_id);
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                }
            }
        }
        else
        {
            ServiceLog.e(TAG, "Update the password fails", request_id);
            JSONObject errorResult = new JSONObject();
            try
            {
                errorResult
                        .put(ResponseKeys.KEY_ERROR_MESSAGE,
                                "Cannot open update the password. Mainly due to the password didn't change!");
            } catch (JSONException e)
            {
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
        }

    }

    /**
     * Check token's validation. 1. Can be decrypted 2. The token doesn't expire
     * 3. The token scope is correct 4. The uid is expected.
     * 
     * Result: 1. Wrong Token, should be totally log out. 2. Token expire, if
     * access_token should be using refresh token to exchange. if refresh_token,
     * then should be totally log out. 3. Token is Valid
     * 
     * @param tokenValidationData
     * @param response
     */
    private void handleTokenValidation(final JSONObject tokenValidationData,
            final HttpServletResponse response)
    {
        final String request_id = getAndLogRequestId(tokenValidationData);

        // Get info from request
        String declare_uid = null;
        String token = null;
        String token_type = null;
        String access_service = null;
        try
        {
            declare_uid = tokenValidationData.getString(RequestKeys.KEY_DECLARE_UID);
            token = tokenValidationData.getString(RequestKeys.KEY_TOKEN);
            token_type = tokenValidationData.getString(RequestKeys.KEY_TOKEN_TYPE);
            access_service = tokenValidationData.getString(RequestKeys.KEY_ACCESS_SERVICE);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when try to get token validation infomation.",
                    request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }
        if (declare_uid == null || token == null || token_type == null || access_service == null)
        {
            ServiceLog.e(TAG, "Missing field. These fields cannot be null", request_id);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }

        TokenVerificationManager verificationManager = new TokenVerificationManager(declare_uid,
                token, token_type, request_id);
        // 1. Try to decode token
        boolean isTokenValid = verificationManager.verifyToken();
        if (!isTokenValid)
        {
            try
            {
                handleAuthFailWithErrorMessage(response, AuthenticationError.ERROR_TOKEN_INVALID,
                        request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // 2. Try to verify expiration
        verificationManager.decomposeToken();
        boolean isTokenExpired = verificationManager.checkTokenExpiration();
        if (isTokenExpired)
        {
            try
            {
                handleAuthFailWithErrorMessage(response, AuthenticationError.ERROR_TOKEN_EXPIRE,
                        request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // 3. Try to verify scope
        boolean isTokenAuthorized = verificationManager.rerifyTokenScope(Scope
                .valueOf(access_service));
        if (!isTokenAuthorized)
        {
            try
            {
                handleAuthFailWithErrorMessage(response, AuthenticationError.ERROR_TOKEN_NO_SCOPE,
                        request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // 4. Try to verify uid
        boolean isUIDValid = verificationManager.verifyUID(declare_uid);
        if (!isUIDValid)
        {
            try
            {
                handleAuthFailWithErrorMessage(response, AuthenticationError.ERROR_TOKEN_NOT_MATCH,
                        request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        try
        {
            handleAuthResult(true, null, response, request_id);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
        }
    }

    /**
     * Token Exchange API.
     * 
     * Client need to send refresh_token, MAP will validate the refresh_token
     * and generate the new access_token.
     * 
     * RefreshToken validation: 1. Can be decrypted 2.The doesn't expire. 3. The
     * uid is expected.
     * 
     * @param tokenExchangeData
     * @param response
     */
    private void handleTokenExchange(final JSONObject tokenExchangeData,
            final HttpServletResponse response)
    {
        final String request_id = getAndLogRequestId(tokenExchangeData);

        // Get info from request
        String declare_uid = null;
        String refresh_token = null;
        String access_token = null;
        try
        {
            declare_uid = tokenExchangeData.getString(RequestKeys.KEY_DECLARE_UID);
            refresh_token = tokenExchangeData.getString(RequestKeys.KEY_REFRESH_TOKEN);
            access_token = tokenExchangeData.getString(RequestKeys.KEY_ACCESS_TOKEN);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when try to get token exchange infomation.",
                    request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }
        if (declare_uid == null || refresh_token == null || access_token == null)
        {
            ServiceLog.e(TAG, "Missing field. These fields cannot be null", request_id);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }

        TokenVerificationManager verificationManager = new TokenVerificationManager(declare_uid,
                refresh_token, TokenVerificationManager.TOKEN_TYPE_REFRESH_TOKEN, request_id);
        // 1. Try to decode token
        boolean isTokenValid = verificationManager.verifyToken();
        if (!isTokenValid)
        {
            try
            {
                handleAuthResult(false,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenInvalid),
                        response, request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // 2. Try to verify expiration
        verificationManager.decomposeToken();
        boolean isTokenExpired = verificationManager.checkTokenExpiration();
        if (isTokenExpired)
        {
            try
            {
                handleAuthResult(
                        false,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenExpiration),
                        response, request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // 3. Try to verify uid
        boolean isUIDValid = verificationManager.verifyUID(declare_uid);
        if (!isUIDValid)
        {
            try
            {
                handleAuthResult(
                        false,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenUidNotMatch),
                        response, request_id);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }
            return;
        }

        // If the refresh is valid, start to refresh the access_token
        ServiceLog.d(TAG, "Refresh token verify success, start to refresh access_token");

        ArrayList<String> sqlResultWithAccessToken = (ArrayList<String>) SQLHelper
                .buildUpdateAccessTokenSQL(declare_uid, access_token);
        String refresh_access_token_sql = sqlResultWithAccessToken.get(0);
        String new_access_token = sqlResultWithAccessToken.get(1);
        boolean result = sAuthDataManager.executeUpdateForLoginStep2(refresh_access_token_sql);
        if (!result)
        {
            ServiceLog.logRequest(TAG, request_id,
                    "Error happend when try to refresh the access_token");
            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            return;
        }

        try
        {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put(ResponseKeys.KEY_ACCESS_TOKEN, new_access_token);
            handleAuthResult(true, jsonResult, response, request_id);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
        }
    }

    private String getAndLogRequestId(JSONObject requestData)
    {
        String requestID = null;
        try
        {
            // If exception happened, just generate a random requestID
            requestID = requestData.getString(RequestKeys.KEY_REQUEST_ID) != null ? requestData
                    .getString(RequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();
            ServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
            ServiceLog.d(TAG, "Received registration data:" + requestData);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            ServiceLog.e(TAG, "JSONException when getting requestID");
            ServiceLog.e(TAG, e.getMessage());
        }
        return requestID;
    }

}
