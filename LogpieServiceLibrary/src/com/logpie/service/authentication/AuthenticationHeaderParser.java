package com.logpie.service.authentication;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.logpie.service.util.ServiceLog;

public class AuthenticationHeaderParser
{
    private static final String TAG = AuthenticationHeaderParser.class.getName();

    public static Map<String, String> getAuthenticationHeader(final HttpServletRequest request)
    {
        Map<String, String> httpHeaders = new HashMap<String, String>();
        Enumeration<String> parameterNames = request.getHeaderNames();
        while (parameterNames.hasMoreElements())
        {
            String paramKey = parameterNames.nextElement();
            String paramValue = request.getHeader(paramKey);
            httpHeaders.put(paramKey, paramValue);
            ServiceLog.d(TAG, "Headers = " + paramKey + ":" + paramValue);
        }
        return httpHeaders;

    }
}
