package com.logpie.authentication.api;


/**
 * This class defined all the APIs AuthenticationService supports
 * 
 * @author yilei
 */
public interface AuthenticationServiceApiDefinition
{
    AuthenticationData authenticateWithEmailAndPassword(final String email, final String password);

    AuthenticationData registerNormalUserAccount(final String email, final String password,
            final String username, final String city_id);

    boolean resetPassword(final String uid, final String oldPassword, final String newPassword);

    boolean deactiviteAccount(final String uid);

    AuthenticationResult verifyToken(final AuthenticationData authData);

    AuthenticationData exchangeToken(final AuthenticationData authData);
}
