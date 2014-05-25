package com.logpie.auth.logic;

import java.util.UUID;

import com.logpie.service.common.helper.CommonServiceLog;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class TokenGenerator
{
    private static final String TAG = TokenGenerator.class.getName();

    /**
     * Build the base key source for AccessToken without any information about token's scope.
     * 
     * @param uid  the user id
     * @return
     */
    public static String generateAccessTokenBaseKeySource(String uid)
    {
        String source = String.format("%s+%s", uid, UUID.randomUUID().toString());
        return source;
    }
    /**
     * Build the base key source for RefreshToken.
     * Containing the currentTimeMillis.
     * @return
     */
    public static String generateRefreshTokenBaseKeySource()
    {
    	String source = String.format("%s+%s", String.valueOf(System.currentTimeMillis()), UUID.randomUUID().toString());
        return source;
    }

    public static String generateToken(String keySource)
    {
        String raw_token = Base64.encode(keySource.getBytes());
        //Base64 will automatically add a new line when the length more than 64.
        //Remove the unnecessary \n
        return raw_token.replace("\n", "");
    }

    public static String decodeToken(String token)
    {
            try
            {
                return new String(Base64.decode(token));
            } catch (Base64DecodingException e)
            {
                CommonServiceLog.e(TAG, "Base64DecodingException when decoing the token");
                e.printStackTrace();
                return null;
            }
    }
}
