package com.logpie.api.exception;

/**
 * This exception is thrown when server side returns unexpected result
 * 
 * @author yilei
 * 
 */
public class LogpieBadResponseException extends LogpieRetryableException
{
    private static final long serialVersionUID = 1L;

    public LogpieBadResponseException(String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieBadResponseException(final Exception exception, final String errorMessage)
    {
        super(exception, errorMessage);
    }

}
