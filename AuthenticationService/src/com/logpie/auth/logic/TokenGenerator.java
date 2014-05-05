package com.logpie.auth.logic;

import java.util.UUID;

import com.logpie.auth.tool.AuthServiceLog;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class TokenGenerator
{
    private static final String TAG = TokenGenerator.class.getName();

    /**
     * Build the base key source without any information about token's scope.
     * 
     * @param email
     * @param password
     * @return
     */
    public static String generateBaseKeySource(String email, String password)
    {
        String source = String.format("%s+%s+%s", email, password, UUID.randomUUID().toString());
        // String token = Base64.encode(source.getBytes());
        return source;
    }

    public static String generateToken(String keySource)
    {
        return Base64.encode(keySource.getBytes());
    }

    public static String decodeToken(String token)
    {
        try
        {
            return new String(Base64.decode(token));
        } catch (Base64DecodingException e)
        {
            AuthServiceLog.e(TAG, "error happend when decode the token");
            AuthServiceLog.e(TAG, e.getMessage());
            return null;
        }
    }
}
