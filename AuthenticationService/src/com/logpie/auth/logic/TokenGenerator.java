package com.logpie.auth.logic;

import java.util.UUID;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class TokenGenerator
{

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
}
