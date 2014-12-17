package com.logpie.api.exception;

/**
 * Exception which are not retryable, like: bad request.
 * 
 * @author yilei
 * 
 */
public class LogpieNonRetryableException extends Exception
{
    private static final long serialVersionUID = 1L;

    public LogpieNonRetryableException(final String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieNonRetryableException(final Exception exception, final String errorMessage)
    {
        super(errorMessage, exception);
    }

}
