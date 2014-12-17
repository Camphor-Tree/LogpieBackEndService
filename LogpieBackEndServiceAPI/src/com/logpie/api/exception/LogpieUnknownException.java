package com.logpie.api.exception;

/**
 * This exception is thrown when there is unknown error happened. All Logpie API
 * should try catch big Exception and then throw this LogpieUnkownException
 * 
 * @author yilei
 * 
 */
public class LogpieUnknownException extends LogpieNonRetryableException
{
    private static final long serialVersionUID = 1L;

    public LogpieUnknownException(Exception exception, String errorMessage)
    {
        super(exception, errorMessage);
    }
}
