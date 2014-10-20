package com.logpie.service.authentication;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.EndPoint.ServiceURL;
import com.logpie.commonlib.RequestKeys;
import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

public class AuthenticationServiceClient
{
    private static final String TAG = AuthenticationServiceClient.class.getName();

    public static abstract class TokenValidationCallback extends ServiceCallback
    {
        public TokenValidationCallback()
        {
        }

        // do the normal flow
        abstract void onSuccess();

        // return 401 to client, it will trigger client to token exchange.
        abstract void onTokenExpiration();

        // do nothing, add metric
        abstract void onTokenFake();

        // do nothing, add metric
        abstract void onTokenUidNotMatch();

        // return 403 to client
        abstract void onNoScope();

        // return 500 to client
        abstract void onUnknownError(final String errorMessage);

        @Override
        public void onSuccess(JSONObject result)
        {
            if (result == null)
            {
                String errorMessage = "The result is null";
                onUnknownError(errorMessage);
                return;
            }
            try
            {
                JSONObject authResponse = result.getJSONObject(GenericConnection.KEY_RESPONSE_DATA);

                if (authResponse == null)
                {
                    String errorMessage = "Auth response is null";
                    onUnknownError(errorMessage);
                    return;
                }

                String authResult;

                authResult = authResponse.getString(ResponseKeys.KEY_AUTHENTICATION_RESULT);

                if (authResult == null)
                {
                    String errorMessage = "Auth result is null";
                    onUnknownError(errorMessage);
                    return;
                }

                if (authResult.equals(ResponseKeys.RESULT_SUCCESS))
                {
                    onSuccess();
                    return;
                }
                else if (authResult.equals(ResponseKeys.RESULT_ERROR))
                {
                    String failReason = authResponse.getString(ResponseKeys.KEY_ERROR_MESSAGE);
                    if (failReason.equals(AuthenticationError.ERROR_TOKEN_EXPIRE))
                    {
                        onTokenExpiration();
                    }
                    else if (failReason.equals(AuthenticationError.ERROR_TOKEN_INVALID))
                    {
                        onTokenFake();
                    }
                    else if (failReason.equals(AuthenticationError.ERROR_TOKEN_NO_SCOPE))
                    {
                        onNoScope();
                    }
                    else if (failReason.equals(AuthenticationError.ERROR_TOKEN_NOT_MATCH))
                    {
                        onTokenUidNotMatch();
                    }
                    else
                    {
                        String errorMessage = "Receiving new fail reason from authentication service:"
                                + failReason;
                        onUnknownError(errorMessage);
                    }

                }
                else
                {
                    String errorMessage = "unknown result from authentication Service:"
                            + authResult.toString();
                    onUnknownError(errorMessage);
                    return;
                }
            } catch (JSONException e)
            {
                String errorMessage = "JSONException when parse the auth result";
                onUnknownError(errorMessage);
                return;
            }
        }

        @Override
        public void onError(JSONObject errorMessage)
        {

        }
    }

    /**
     * used to validate the token
     * 
     * @param declare_uid
     *            they uid declared to be.
     * @param access_token
     *            access_token
     * @param serviceName
     *            the name of the service which is accessed
     */
    public void validateToken(final String declare_uid, final String access_token,
            final String serviceName, final TokenValidationCallback callback)
    {
        final GenericConnection connection = new GenericConnection();
        connection.initialize(ServiceURL.AuthenticationService);

        JSONObject tokenValidationData = new JSONObject();
        try
        {
            tokenValidationData.put(RequestKeys.KEY_REQUEST_TYPE, "TOKEN_VALIDATION");
            tokenValidationData.put(RequestKeys.KEY_DECLARE_UID, declare_uid);
            tokenValidationData.put(RequestKeys.KEY_TOKEN, access_token);
            tokenValidationData.put(RequestKeys.KEY_TOKEN_TYPE, "access_token");
            tokenValidationData.put(RequestKeys.KEY_ACCESS_SERVICE, serviceName);
            ServiceLog.d(TAG, "Register String" + tokenValidationData.toString());
            connection.setRequestData(tokenValidationData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        connection.send(callback);
    }
}
