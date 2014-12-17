package com.logpie.authentication.api;

import com.logpie.api.exception.LogpieBadRequestException;
import com.logpie.api.exception.LogpieBadResponseException;
import com.logpie.api.exception.LogpieConnectionException;
import com.logpie.api.exception.LogpieServiceErrorException;
import com.logpie.api.exception.LogpieUnknownException;

/**
 * This class defined all the APIs AuthenticationService supports
 * 
 * @author yilei
 */
public interface AuthenticationServiceApiDefinition
{
    AuthenticationData authenticateWithEmailAndPassword(final String email, final String password)
            throws LogpieBadRequestException, LogpieUnknownException, LogpieConnectionException,
            LogpieBadResponseException, LogpieServiceErrorException;

    AuthenticationData registerNormalUserAccount(final String email, final String password,
            final String username, final String city_id);

    boolean resetPassword(final String uid, final String oldPassword, final String newPassword);

    boolean deactiviteAccount(final String uid);

    AuthenticationResult verifyToken(final AuthenticationData authData);

    AuthenticationData exchangeToken(final AuthenticationData authData);
}
