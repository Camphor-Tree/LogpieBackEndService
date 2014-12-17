package com.logpie.api.exception;

/**
 * This exception is thrown when server side internal error happens
 * 
 * @author yilei
 * 
 */
public class LogpieServiceErrorException extends LogpieRetryableException
{
    private static final long serialVersionUID = 1L;

    public LogpieServiceErrorException(String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieServiceErrorException(final Exception exception, final String errorMessage)
    {
        super(exception, errorMessage);
    }

}
