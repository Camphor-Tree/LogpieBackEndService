package com.logpie.service.authentication;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logpie.service.authentication.AuthenticationServiceClient.TokenValidationCallback;
import com.logpie.service.error.ErrorType;
import com.logpie.service.logic.helper.ManagerHelper;
import com.logpie.service.util.ServiceLog;

/**
 * This class is used to handle the authentication
 * 
 * @author yilei
 * 
 */
public class AuthRequestHandler
{
    private static final String TAG = AuthRequestHandler.class.getName();

    private volatile boolean mAuthResult = false;

    public AuthRequestHandler()
    {

    }

    public boolean handleAuthentication(final HttpServletRequest request,
            final HttpServletResponse response, final String serviceName)
    {
        Map<String, String> headers = AuthenticationHeaderParser.getAuthenticationHeader(request);
        ServiceLog.d(TAG, "Header Size:" + headers);

        String access_token = headers.get("access_token");
        String uid = headers.get("uid");

        if (uid == null || access_token == null || uid.equals("") || access_token.equals(""))
        {
            ServiceLog.e(TAG, "Authentication header is null!");
            ManagerHelper.handleResponseWithError(response, ErrorType.AUTH_ERROR);
            return false;
        }

        AuthenticationServiceClient authClient = new AuthenticationServiceClient();
        TokenValidationCallback callback = new AuthenticationServiceClient.TokenValidationCallback()
        {

            @Override
            void onUnknownError(String errorMessage)
            {
                mAuthResult = false;
                ServiceLog.e(TAG, errorMessage);
                ManagerHelper.handleResponseWithError(response, ErrorType.SEVER_ERROR);

            }

            @Override
            void onTokenUidNotMatch()
            {
                // TODO: Metric: we should add metric alarm here.
                mAuthResult = false;
                ServiceLog.e(TAG,
                        "The token uid doesn't match the declare uid! Logpie may under attack");
            }

            @Override
            void onTokenFake()
            {
                // TODO: Metric: we should add metric alarm here.
                mAuthResult = false;
                ServiceLog.e(TAG, "The token is fake! Logpie may under attack!");
            }

            @Override
            void onTokenExpiration()
            {
                mAuthResult = false;
                ManagerHelper.handleResponseWithError(response, ErrorType.TOKEN_EXPIRE);
            }

            @Override
            void onSuccess()
            {
                mAuthResult = true;
            }

            @Override
            void onNoScope()
            {
                mAuthResult = false;
                ManagerHelper.handleResponseWithError(response, ErrorType.AUTH_ERROR);
            }
        };
        authClient.validateToken(uid, access_token, serviceName, callback);
        return mAuthResult;
    }
}
