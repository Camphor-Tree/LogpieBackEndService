package com.logpie.api.exception;

/**
 * This Exception is thrown when the parameters are not correct.
 * 
 * @author yilei
 * 
 */
public class LogpieBadRequestException extends LogpieNonRetryableException
{
    private static final long serialVersionUID = 1L;

    public LogpieBadRequestException(String errorMessage)
    {
        super(errorMessage);
    }

    public LogpieBadRequestException(final Exception exception, final String errorMessage)
    {
        super(exception, errorMessage);
    }

}
