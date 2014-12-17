package com.logpie.authentication.api.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.api.support.connection.RequestKeys;
import com.logpie.api.support.connection.ResponseKeys;
import com.logpie.api.template.LogpieWebServiceCall;
import com.logpie.authentication.api.support.exception.BadResponseException;

public class AuthenticationServiceCall
{
    public static class AuthenticationCall extends LogpieWebServiceCall<Map<String, String>>
    {
        private final String mEmail;
        private final String mPassword;

        public AuthenticationCall(final String email, final String password)
        {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected JSONObject getRequestJSON()
        {
            JSONObject authenticateData = new JSONObject();
            try
            {
                authenticateData.put(RequestKeys.KEY_REQUEST_TYPE, "AUTHENTICATE");
                authenticateData.put(RequestKeys.KEY_EMAIL, mEmail);
                authenticateData.put(RequestKeys.KEY_PASSWORD, mPassword);
                authenticateData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            } catch (JSONException e)
            {
                e.printStackTrace();
                return null;
            }
            return authenticateData;
        }

        @Override
        protected Map<String, String> parseResponseJSON(JSONObject authResult)
                throws BadResponseException
        {
            if (authResult == null)
            {
                throw new BadResponseException(
                        "Null response from AuthenticationService authenticate");
            }
            Map<String, String> authDataMap = new HashMap<String, String>();
            try
            {
                String accessToken = authResult.getString(ResponseKeys.KEY_ACCESS_TOKEN);
                String refreshToken = authResult.getString(ResponseKeys.KEY_REFRESH_TOKEN);
                String uid = authResult.getString(ResponseKeys.KEY_UID);
                authDataMap.put(
                        AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_ACCESS_TOKEN,
                        accessToken);
                authDataMap.put(
                        AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_REFRESH_TOKEN,
                        refreshToken);
                authDataMap.put(AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_UID,
                        uid);
                authDataMap.put(AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_EMAIL,
                        mEmail);

            } catch (JSONException e)
            {
                e.printStackTrace();
                throw new BadResponseException("Bad response from AuthenticationService:"
                        + authResult.toString());
            }
            return authDataMap;
        }

    }
}
