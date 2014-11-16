package com.logpie.authentication.api;

import com.logpie.authentication.api.support.AuthenticationServiceClientHandler;

/**
 * AuthenticationServiceClient is the only API class should be used in Logpie
 * Project to call AuthenticationService. This client class is designed to help
 * you call AuthenticationService. End-To-End test should also call this class
 * to test.
 * 
 * All these API are synchronous API. If you are in the Android platform, please
 * make sure don't call these API on main thread.
 * 
 * @author yilei
 * 
 */
public class AuthenticationServiceClient implements AuthenticationServiceApiDefinition
{

    @Override
    public AuthenticationData authenticateWithEmailAndPassword(String email, String password)
    {
        new AuthenticationServiceClientHandler();
        return null;
    }

    @Override
    public AuthenticationData registerNormalUserAccount(String email, String password,
            String username, String city_id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean resetPassword(String uid, String oldPassword, String newPassword)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deactiviteAccount(String uid)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AuthenticationResult verifyToken(AuthenticationData authData)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AuthenticationData exchangeToken(AuthenticationData authData)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
