package com.logpie.auth.logic;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import com.logpie.service.common.error.ErrorMessage;
import com.logpie.service.common.error.ErrorType;
import com.logpie.service.common.exception.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;
import com.logpie.service.common.helper.HttpResponseWriter;
import com.logpie.service.common.helper.LatencyHelper;
import com.logpie.service.common.helper.RequestKeys;
import com.logpie.service.common.helper.ResponseKeys;
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

    // private static ServletContext sGlobalUniqueContext;
    private static AuthDataManager sAuthDataManager;

    public static void initialize(ServletContext globalUniqueContext)
    {
        // sGlobalUniqueContext = globalUniqueContext;
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
        JSONObject postBody = null;
        try
        {
            postBody = HttpRequestParser.httpRequestParser(request);
        } catch (HttpRequestIsNullException e)
        {
            CommonServiceLog.e(TAG, "The coming request is null!It should be a bug of tomcat!", e);
            return;
        }
        if (postBody != null)
        {
            CommonServiceLog.d(TAG, "[Receiving request data:]" + postBody.toString());
            AuthenticationType type = getAuthenticationType(postBody);
            if (type == null)
            {
                CommonServiceLog.d(TAG, "authentication type is null!");
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
                break;
            case FORGET_PASSWORD:
                break;
            case TOKEN_VALIDATION:
            {
                handleTokenValidation(postBody, response);
                break;
            }
            default:
            {
                CommonServiceLog.e(TAG, "Unsupported Type!");
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
            CommonServiceLog.e(
                    TAG,
                    "Returning error code when handling authentication ->" + "ErrorCode:"
                            + errorType.getErrorCode() + " ErrorMessage:"
                            + errorType.getErrorMessage(), requestId);
        } catch (IOException e)
        {
            CommonServiceLog.e(TAG, "IOException happend when sendErrorCode", e);
        }
    }

    private AuthenticationType getAuthenticationType(JSONObject postData)
    {
        if (postData.has(RequestKeys.KEY_AUTHENTICATION_TYPE))
        {
            try
            {
                return AuthenticationType.matchType(postData
                        .getString(RequestKeys.KEY_AUTHENTICATION_TYPE));
            } catch (JSONException e)
            {
                CommonServiceLog
                        .e(TAG, "JSONException happend when parsing Authentication Type", e);
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
            if (isUserExisted)
            {
                CommonServiceLog.e(TAG, "email already exist", request_id);
                JSONObject errorMessage = new JSONObject();
                try
                {
                    errorMessage.put(ResponseKeys.KEY_ERROR_MESSAGE,
                            ErrorMessage.ERROR_EMAIL_ALREADY_EXIST);
                    handleAuthResult(false, errorMessage, response);
                } catch (JSONException e1)
                {
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                }
            }
            else
            {

                // Step 1: just insert into the database and get the uid back
                sql = SQLHelper.buildCreateUserStep1SQL(email, password);
                sAuthDataManager.executeInsertAndGetUIDandEmail(sql, new DataCallback()
                {
                    @Override
                    public void onSuccess(JSONObject result)
                    {
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
                            CommonServiceLog.d(TAG, "Generating the new uid:" + uid, request_id);
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
                                        userNickName, city);
                                CommonServiceLog.d(TAG, "Update token successfully", request_id);
                                result.put(ResponseKeys.KEY_ACCESS_TOKEN, resultArray.get(1));
                                result.put(ResponseKeys.KEY_REFRESH_TOKEN, resultArray.get(2));
                                handleAuthResult(true, result, response);
                            }
                            else
                            {
                                CommonServiceLog.d(TAG, "Update token fail", request_id);
                                handleAuthenticationResponseWithError(response,
                                        ErrorType.SEVER_ERROR, request_id);
                                return;
                            }
                        } catch (JSONException e)
                        {
                            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
                            CommonServiceLog.e(TAG, ":JSONException when getting insert result",
                                    request_id, e);
                            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                                    request_id);
                            return;
                        }

                    }

                    @Override
                    public void onError(JSONObject error)
                    {
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
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
            CommonServiceLog.e(TAG, "JSONException when getting email/password", request_id, e);

        } catch (EmailAlreadyExistException e)
        {
            CommonServiceLog.e(TAG, "email already exist", e);
            JSONObject errorMessage = new JSONObject();
            try
            {
                errorMessage.put(ResponseKeys.KEY_ERROR_MESSAGE,
                        ErrorMessage.ERROR_EMAIL_ALREADY_EXIST);
                handleAuthResult(false, errorMessage, response);
            } catch (JSONException e1)
            {
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            }

        }

    }

    private void handleAuthResult(final boolean success, JSONObject data,
            final HttpServletResponse response) throws JSONException
    {
        if (data == null)
        {
            data = new JSONObject();
        }
        CommonServiceLog.d(TAG, data.toString());
        if (success)
        {
            data.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, ResponseKeys.KEY_RESULT_SUCCESS);
        }
        else
        {
            data.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, ResponseKeys.KEY_RESULT_ERROR);
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
            CommonServiceLog.d(TAG, "currentTime:" + currentTime);
            CommonServiceLog
                    .d(TAG, "access_token_expiration:" + access_token_expiration.toString());
            CommonServiceLog.d(TAG,
                    "refresh_token_expiration:" + refresh_token_expiration.toString());
            if (!access_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            {
                CommonServiceLog.d(TAG, "access_token already expire, refresh the access_token");
                // (Step 3.1): If the access_token already expire, update the
                // token for the user.
                ArrayList<String> sqlResultWithAccessToken = (ArrayList<String>) SQLHelper
                        .buildUpdateAccessTokenSQL(uid, access_token);
                String sql_step3_1 = sqlResultWithAccessToken.get(0);
                access_token = sqlResultWithAccessToken.get(1);
                boolean step3_1_result = sAuthDataManager.executeQueryForLoginStep2(sql_step3_1);
                if (!step3_1_result)
                {
                    CommonServiceLog.e(sql,
                            "Error happend in authentication Step2, refresh the access_token",
                            request_id);
                    CommonServiceLog.logRequest(TAG, request_id,
                            "Error happend in authentication Step2, refresh the access_token");
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                    return;
                }
            }
            else
            {
                CommonServiceLog
                        .d(TAG,
                                "access_token still valid, won't refresh to disturb the account in other device",
                                request_id);
            }
            if (!refresh_token_expiration.before(TimeHelper.getCurrentTimestamp()))
            {
                CommonServiceLog.d(TAG, "refresh_token already expire, refresh the refresh_token",
                        request_id);
                // (Step 3.2): If the refresh_token already expire, update the
                // token for the user.
                ArrayList<String> sqlResultWithRefreshToken = (ArrayList<String>) SQLHelper
                        .buildUpdateRefreshTokenSQL(uid, access_token);
                String sql_step3_2 = sqlResultWithRefreshToken.get(0);
                refresh_token = sqlResultWithRefreshToken.get(1);
                boolean step3_2_result = sAuthDataManager.executeQueryForLoginStep2(sql_step3_2);
                if (!step3_2_result)
                {
                    CommonServiceLog.e(sql,
                            "Error happend in authentication Step3, refresh the refresh_token",
                            request_id);
                    CommonServiceLog.logRequest(TAG, request_id,
                            "Error happend in authentication Step3, refresh the refresh_token");
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                    return;
                }
            }
            else
            {
                CommonServiceLog
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
                handleAuthResult(true, authResult, response);
            } catch (JSONException e)
            {
                handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
                CommonServiceLog.e(TAG,
                        "Authenticate Step4, JSONException when getting insert result", e);
            }

        } catch (JSONException e)
        {
            handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR, request_id);
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            CommonServiceLog.e(TAG, "JSONException when getting email/password", request_id, e);
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
            email = resetPasswordData.getString(RequestKeys.KEY_PASSWORD);
            oldPassword = resetPasswordData.getString(RequestKeys.KEY_KEYWORD);
            newPassword = resetPasswordData.getString(RequestKeys.KEY_VALUE);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException when try to get reset password infomation.",
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
            CommonServiceLog.e(TAG, "JSONException when try to get uid.", request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
        }
        if (uid == null)
        {
            CommonServiceLog.e(TAG, "uid must not be null!", request_id);
            handleAuthenticationResponseWithError(response, ErrorType.AUTH_ERROR, request_id);
        }
        String resetPasswordSQL = SQLHelper.buildResetPasswordSQL(uid, newPassword);
        boolean resetSuccess = sAuthDataManager.executeUpdate(resetPasswordSQL);
        if (resetSuccess)
        {
            try
            {
                // reset password just sendback the success signal
                handleAuthResult(true, null, response);
            } catch (JSONException e)
            {
                // retry once.
                // reset password just sendback the success signal
                try
                {
                    handleAuthResult(true, null, response);
                } catch (JSONException e1)
                {
                    CommonServiceLog.e(TAG, "JSONException when try to put success signal",
                            request_id);
                    handleAuthenticationResponseWithError(response, ErrorType.SEVER_ERROR,
                            request_id);
                }
            }
        }
        else
        {
            CommonServiceLog.e(TAG, "Update the password fails", request_id);
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
     * Check token's validation. 1. Can be decrypted 2. The uid is expected. 3.
     * The doesn't expire.
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
            CommonServiceLog.e(TAG, "JSONException when try to get token validation infomation.",
                    request_id, e);
            handleAuthenticationResponseWithError(response, ErrorType.BAD_REQUEST, request_id);
        }
        if (declare_uid == null || token == null || token_type == null || access_service == null)
        {
            CommonServiceLog.e(TAG, "Missing field. These fields cannot be null", request_id);
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
                handleAuthResult(false,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenInvalid),
                        response);
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
            }
            return;
        }

        // 2. Try to verify expiration
        boolean isTokenExpired = verificationManager.checkTokenExpiration();
        if (isTokenExpired)
        {
            try
            {
                handleAuthResult(
                        true,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenExpiration),
                        response);
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
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
                handleAuthResult(true,
                        verificationManager
                                .buildFailJSON(TokenVerificationManager.sFailReasonTokenNoScope),
                        response);
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
            }
            return;
        }

        try
        {
            handleAuthResult(true, verificationManager.buildSuccessJSON(), response);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException when handle auth result", request_id, e);
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
            CommonServiceLog.d(TAG, "Start handling register: requestID=" + requestID);
            CommonServiceLog.d(TAG, "Received registration data:" + requestData);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            CommonServiceLog.e(TAG, "JSONException when getting requestID");
            CommonServiceLog.e(TAG, e.getMessage());
        }
        return requestID;
    }

}
