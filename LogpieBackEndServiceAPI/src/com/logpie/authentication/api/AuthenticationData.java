package com.logpie.authentication.api;

import java.util.Map;

import com.logpie.authentication.api.support.AuthenticationServiceAPIConstants;

/**
 * AuthenticationData is the class to encapsulate all the auth related fields:
 * uid, email, access_token, refresh_token.
 * 
 * @author yilei
 * 
 */
public class AuthenticationData
{
    public static final String KEY_AUTH_DATA_UID = AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_UID;
    public static final String KEY_AUTH_DATA_EMAIL = AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_EMAIL;
    public static final String KEY_AUTH_DATA_ACCESS_TOKEN = AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_ACCESS_TOKEN;
    public static final String KEY_AUTH_DATA_REFRESH_TOKEN = AuthenticationServiceAPIConstants.AuthenticationDataKeys.KEY_REFRESH_TOKEN;

    String mUid;
    String mEmail;
    String mAccessToken;
    String mRefreshToken;

    public AuthenticationData(final String uid, final String email, final String access_token,
            final String refresh_token)
    {
        mUid = uid;
        mEmail = email;
        mAccessToken = access_token;
        mRefreshToken = refresh_token;
    }

    public AuthenticationData(final String uid, final String email)
    {
        this(uid, email, null, null);
    }

    /**
     * Help build the AuthenticationData object
     * 
     * @param authDataMap
     * @return
     */
    public static AuthenticationData buildAuthenticationData(final Map<String, String> authDataMap)
    {
        if (authDataMap == null)
        {
            return null;
        }
        String uid = authDataMap.get(KEY_AUTH_DATA_UID);
        String email = authDataMap.get(KEY_AUTH_DATA_EMAIL);
        String access_token = authDataMap.get(KEY_AUTH_DATA_ACCESS_TOKEN);
        String refresh_token = authDataMap.get(KEY_AUTH_DATA_REFRESH_TOKEN);

        return new AuthenticationData(uid, email, access_token, refresh_token);
    }

    public AuthenticationData(final String uid, final String email, final String access_token)
    {
        this(uid, email, access_token, null);
    }

    public String getUid()
    {
        return mUid;
    }

    public void setUid(String uid)
    {
        mUid = uid;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String email)
    {
        mEmail = email;
    }

    public String getAccessToken()
    {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken)
    {
        mAccessToken = accessToken;
    }

    public String getRefreshToken()
    {
        return mRefreshToken;
    }

    public void setRefreshToken(String refreshToken)
    {
        mRefreshToken = refreshToken;
    }

}
