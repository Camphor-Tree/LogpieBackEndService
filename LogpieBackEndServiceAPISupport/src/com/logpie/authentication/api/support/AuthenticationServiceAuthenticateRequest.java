package com.logpie.authentication.api.support;

/**
 * Used to handle the authentication request
 * 
 * @author yilei
 */
public class AuthenticationServiceAuthenticateRequest
{
    private String mEmail;
    private String mPassword;

    public AuthenticationServiceAuthenticateRequest(final String email, final String password)
    {
        mEmail = email;
        mPassword = password;
    }

}
