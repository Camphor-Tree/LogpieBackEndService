package com.logpie.customer.tool;

import javax.servlet.http.HttpServletResponse;

public enum CustomerErrorType
{
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "bad request"),

    SEVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server internal error"),

    AUTH_ERROR(HttpServletResponse.SC_FORBIDDEN, "cannot authenticate the user");

    private int errorCode;
    private String errorMessage;

    CustomerErrorType(int code, String message)
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
