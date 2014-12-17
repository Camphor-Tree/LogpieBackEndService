package com.logpie.authentication.api.support.exception;

public class BadRequestException extends Exception
{
    private static final long serialVersionUID = 1L;

    public BadRequestException(final String errorMessage)
    {
        super(errorMessage);
    }
}
