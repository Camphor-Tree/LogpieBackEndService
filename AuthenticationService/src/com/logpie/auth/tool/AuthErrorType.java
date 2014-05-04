package com.logpie.auth.tool;

import javax.servlet.http.HttpServletResponse;

public enum AuthErrorType
{
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "bad request"),

    SEVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server internal error"),

    AUTH_ERROR(HttpServletResponse.SC_FORBIDDEN, "cannot authenticate the user");

    private int errorCode;
    private String errorMessage;

    AuthErrorType(int code, String message)
    {
        this.errorCode = code;
        this.errorMessage = message;
    }

    public int getErrorCode()
    {
        return this.errorCode;
    }

    public String getErrorMessage()
    {
        return this.errorMessage;
    }
}
