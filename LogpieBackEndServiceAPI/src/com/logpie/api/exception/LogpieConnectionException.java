package com.logpie.api.exception;

/**
 * This exception is thrown when there is connection problem, like IOException
 * 
 * @author yilei
 * 
 */
public class LogpieConnectionException extends LogpieRetryableException
{
    private static final long serialVersionUID = 1L;

    public LogpieConnectionException(String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieConnectionException(final Exception exception, final String errorMessage)
    {
        super(exception, errorMessage);
    }

}
