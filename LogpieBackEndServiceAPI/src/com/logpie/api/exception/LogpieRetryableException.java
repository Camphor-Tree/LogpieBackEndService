package com.logpie.api.exception;

/**
 * Exception which are retryable, like IOException, Service internal error.
 * 
 * @author yilei
 * 
 */
public class LogpieRetryableException extends Exception
{
    private static final long serialVersionUID = 1L;

    public LogpieRetryableException(final String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieRetryableException(final Exception exception, final String errorMessage)
    {
        super(errorMessage, exception);
    }
}
