package com.logpie.service.authentication;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationHeaderParser
{

    public static Map<String, String> getAuthenticationHeader(final HttpServletRequest request)
    {
        Map<String, String> httpHeaders = new HashMap<String, String>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements())
        {
            String paramKey = parameterNames.nextElement();
            String paramValue = request.getParameter(paramKey);
            httpHeaders.put(paramKey, paramValue);
        }
        return httpHeaders;

    }
}
