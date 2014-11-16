package com.logpie.authentication.api;

/**
 * AuthenticationData is the class to encapsulate all the auth related fields:
 * uid, email, access_token, refresh_token.
 * 
 * @author yilei
 * 
 */
public class AuthenticationData
{
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
