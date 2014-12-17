package com.logpie.api.template;

import org.json.JSONObject;

import com.logpie.api.support.connection.AuthType;
import com.logpie.api.support.connection.EndPoint.ServiceURL;
import com.logpie.api.support.connection.GenericConnection;
import com.logpie.authentication.api.support.exception.BadRequestException;
import com.logpie.authentication.api.support.exception.BadResponseException;
import com.logpie.authentication.api.support.exception.ConnectionException;
import com.logpie.authentication.api.support.exception.ServerInternalException;

/**
 * 
 * This is the template for Logpie webservice call. Child class should implement
 * the getRequestJSON() and parseResponseJSON().
 * 
 * @author yilei
 * 
 * @param <T>
 *            T is the return type.
 */
public abstract class LogpieWebServiceCall<T>
{
    private GenericConnection mConnection;

    public T serviceCall(final ServiceURL url, final AuthType authType, final String accessToken,
            final String refreshToken, final String uid) throws BadRequestException,
            ConnectionException, BadResponseException, ServerInternalException
    {
        JSONObject requestJSON = getRequestJSON();
        // Build connection
        getConnection();
        // Setup connection parameters
        setUpConnectionParameter(url, authType, accessToken, refreshToken, uid);
        // Set request json.
        mConnection.setRequestData(requestJSON);
        // Get response json
        JSONObject responseJSON = mConnection.send();
        // Parse response
        return parseResponseJSON(responseJSON);

    }

    private void getConnection()
    {
        mConnection = new GenericConnection();
    }

    private void setUpConnectionParameter(final ServiceURL url, final AuthType authType,
            final String accessToken, final String refreshToken, final String uid)
    {
        mConnection.initialize(ServiceURL.AuthenticationService, AuthType.NoAuth, accessToken,
                refreshToken, uid);
    }

    protected abstract JSONObject getRequestJSON();

    protected abstract T parseResponseJSON(final JSONObject responseJSON)
            throws BadResponseException;

}
