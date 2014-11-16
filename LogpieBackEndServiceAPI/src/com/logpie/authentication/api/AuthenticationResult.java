package com.logpie.authentication.api;

/**
 * The AuthenticationResult is the wrapper for the result of verifyToken()
 * 
 * @author yilei
 * 
 */
public class AuthenticationResult
{
    boolean mIsSuccess;
    AuthenticationFailReasonEnum mFailReason;

    private AuthenticationResult(final boolean isSuccess,
            final AuthenticationFailReasonEnum failReason)
    {
        mIsSuccess = isSuccess;
        mFailReason = failReason;
    }

    public static AuthenticationResult buildSuccessResult(final AuthenticationData authData)
    {
        return new AuthenticationResult(true, null);
    }

    public static AuthenticationResult buildFailResult(final AuthenticationFailReasonEnum failReason)
    {
        return new AuthenticationResult(false, failReason);
    }

    public boolean isIsSuccess()
    {
        return mIsSuccess;
    }

    public void setIsSuccess(boolean isSuccess)
    {
        mIsSuccess = isSuccess;
    }

    public AuthenticationFailReasonEnum getFailReason()
    {
        return mFailReason;
    }

    public void setFailReason(AuthenticationFailReasonEnum failReason)
    {
        mFailReason = failReason;
    }

}
