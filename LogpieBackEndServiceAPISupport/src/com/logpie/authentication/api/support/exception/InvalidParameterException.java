package com.logpie.authentication.api.support.exception;

/**
 * API support will throw InvalidParameterException when the parameters are
 * illegal.
 * 
 * @author yilei
 * 
 */
public class InvalidParameterException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InvalidParameterException(final String errorMessage)
    {
        super(errorMessage);
    }

}
