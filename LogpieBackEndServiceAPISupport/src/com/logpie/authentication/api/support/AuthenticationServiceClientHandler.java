package com.logpie.authentication.api.support;

import java.util.Map;

import com.logpie.api.support.connection.AuthType;
import com.logpie.api.support.connection.EndPoint.ServiceURL;
import com.logpie.authentication.api.support.AuthenticationServiceCall.AuthenticationCall;
import com.logpie.authentication.api.support.exception.BadRequestException;
import com.logpie.authentication.api.support.exception.BadResponseException;
import com.logpie.authentication.api.support.exception.ConnectionException;
import com.logpie.authentication.api.support.exception.InvalidParameterException;
import com.logpie.authentication.api.support.exception.ServerInternalException;

public class AuthenticationServiceClientHandler
{
    ServiceCall mCall;

    // for unit test only
    public AuthenticationServiceClientHandler(ServiceCall call)
    {
        mCall = call;
    }

    public AuthenticationServiceClientHandler()
    {
        mCall = new ServiceCall();
    }

    public String getAuthenticateResult()
    {
        return mCall.call();
    }

    public Map<String, String> authenticateWithEmailAndPassword(final String email,
            final String password) throws InvalidParameterException, BadRequestException,
            ConnectionException, BadResponseException, ServerInternalException
    {
        // check parameter legal
        checkEmailAndPassword(email, password);

        AuthenticationCall authenticationCall = new AuthenticationCall(email, password);
        return authenticationCall.serviceCall(ServiceURL.AuthenticationService, AuthType.NoAuth,
                null, null, null);
    }

    public Map<String, String> registerNormalUserAccount(final String email, final String password,
            final String username, final String city_id)
    {
        return null;
    }

    public boolean resetPassword(final String uid, final String oldPassword,
            final String newPassword)
    {
        return false;
    }

    public boolean deactiviteAccount(final String uid)
    {
        return false;
    }

    public Map<String, String> verifyToken(final Map<String, String> authData)
    {
        return null;
    }

    public Map<String, String> exchangeToken(final Map<String, String> authData)
    {
        return null;
    }

    private void checkEmailAndPassword(final String email, final String password)
            throws InvalidParameterException
    {
        if (!AuthenticationServiceParameterValidator.nonNullCheck(email, password))
        {
            throw new InvalidParameterException("email or password is null");
        }

        if (!AuthenticationServiceParameterValidator.isValidEmail(email))
        {
            throw new InvalidParameterException("Email format is not correct");
        }

        if (!AuthenticationServiceParameterValidator.isValidPassword(password))
        {
            throw new InvalidParameterException("Password format is not correct");
        }
    }

}
