package com.logpie.authentication.api.support.exception;

public class ServerInternalException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ServerInternalException(final String errorMessage)
    {
        super(errorMessage);
    }
}
