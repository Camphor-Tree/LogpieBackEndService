package com.logpie.authentication.api.support.exception;

public class ConnectionException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ConnectionException(final String errorMessage)
    {
        super(errorMessage);
    }
}
