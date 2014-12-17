package com.logpie.authentication.api;

import java.util.Map;

import com.logpie.api.exception.LogpieBadRequestException;
import com.logpie.api.exception.LogpieBadResponseException;
import com.logpie.api.exception.LogpieConnectionException;
import com.logpie.api.exception.LogpieServiceErrorException;
import com.logpie.api.exception.LogpieUnknownException;
import com.logpie.authentication.api.support.AuthenticationServiceClientHandler;
import com.logpie.authentication.api.support.exception.BadRequestException;
import com.logpie.authentication.api.support.exception.BadResponseException;
import com.logpie.authentication.api.support.exception.ConnectionException;
import com.logpie.authentication.api.support.exception.InvalidParameterException;
import com.logpie.authentication.api.support.exception.ServerInternalException;

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
    private final AuthenticationServiceClientHandler mHandler;

    public AuthenticationServiceClient()
    {
        mHandler = new AuthenticationServiceClientHandler();
    }

    @Override
    public AuthenticationData authenticateWithEmailAndPassword(final String email,
            final String password) throws LogpieBadRequestException, LogpieUnknownException,
            LogpieConnectionException, LogpieBadResponseException, LogpieServiceErrorException
    {
        try
        {
            Map<String, String> authDataMap;
            authDataMap = mHandler.authenticateWithEmailAndPassword(email, password);
            return AuthenticationData.buildAuthenticationData(authDataMap);
        } catch (InvalidParameterException e)
        {
            e.printStackTrace();
            throw new LogpieBadRequestException(e, "InvalidParameter");
        } catch (BadRequestException e)
        {
            e.printStackTrace();
            throw new LogpieBadRequestException(e, "Bad request");
        } catch (ConnectionException e)
        {
            e.printStackTrace();
            throw new LogpieConnectionException(e, "Connection problem");
        } catch (BadResponseException e)
        {
            e.printStackTrace();
            throw new LogpieBadResponseException(e, "Bad response from server");
        } catch (ServerInternalException e)
        {
            e.printStackTrace();
            throw new LogpieServiceErrorException(e, "Bad response from server");
        } catch (Exception e)
        {
            throw new LogpieUnknownException(e,
                    "Unkown exception happens when calling authenticateWithEmailAndPassword");
        }
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
