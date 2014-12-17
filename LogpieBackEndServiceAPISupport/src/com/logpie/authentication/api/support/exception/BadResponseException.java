package com.logpie.authentication.api.support.exception;

public class BadResponseException extends Exception
{
    private static final long serialVersionUID = 1L;

    public BadResponseException(final String errorMessage)
    {
        super(errorMessage);
    }
}
