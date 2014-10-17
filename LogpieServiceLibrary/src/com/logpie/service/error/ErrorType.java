package com.logpie.service.error;

import javax.servlet.http.HttpServletResponse;

public enum ErrorType
{
    // 400
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "bad request"),

    // 500
    SEVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server internal error"),

    // 403
    AUTH_ERROR(HttpServletResponse.SC_FORBIDDEN, "cannot authenticate the user"),

    // 401
    TOKEN_EXPIRE(HttpServletResponse.SC_UNAUTHORIZED, "token get expired");

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
