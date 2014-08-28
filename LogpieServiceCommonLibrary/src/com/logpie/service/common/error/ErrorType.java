package com.logpie.service.common.error;

import javax.servlet.http.HttpServletResponse;

public enum ErrorType
{
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "bad request"),

    SEVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server internal error"),

    AUTH_ERROR(HttpServletResponse.SC_FORBIDDEN, "cannot authenticate the user");

    private int errorCode;
    private String errorMessage;

    ErrorType(int code, String message)
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
