package com.logpie.authentication.api.support;

import java.util.Map;

import com.logpie.authentication.api.support.exception.InvalidParameterException;

public class AuthenticationServiceClientHandler
{
    public AuthenticationServiceClientHandler()
    {

    }

    public Map<String, String> authenticateWithEmailAndPassword(String email, String password)
            throws InvalidParameterException
    {
        if (!AuthenticationServiceParameterValidator.nonNullCheck(email, password))
        {
            throw new InvalidParameterException();
        }
        return null;
    }

    public Map<String, String> registerNormalUserAccount(String email, String password,
            String username, String city_id)
    {
        return null;
    }

    public boolean resetPassword(String uid, String oldPassword, String newPassword)
    {
        return false;
    }

    public boolean deactiviteAccount(String uid)
    {
        return false;
    }

    public Map<String, String> verifyToken(Map<String, String> authData)
    {
        return null;
    }

    public Map<String, String> exchangeToken(Map<String, String> authData)
    {
        return null;
    }

}
