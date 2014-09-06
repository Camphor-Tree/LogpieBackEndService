package com.logpie.auth.logic;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.logpie.auth.security.AbstractDataEncryptor;
import com.logpie.auth.security.TokenEncryptor;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.TimeHelper;

public class TokenGenerator
{
    private static final String TAG = TokenGenerator.class.getName();
    private static final AbstractDataEncryptor sEncryptor = new TokenEncryptor();

    /**
     * Build the base key source for AccessToken without any information about
     * token's scope.
     * 
     * @param uid
     *            the user id
     * @return
     */
    public static String generateAccessTokenBaseKeySource(String uid)
    {
        String source = String.format("%s+%s#%s", uid, TimeHelper.getCurrentTimestamp().toString(),
                getRandomUUIDWithoutDash());
        return source;
    }

    /**
     * Build the base key source for RefreshToken. Containing the
     * currentTimeMillis.
     * 
     * @return
     */
    public static String generateRefreshTokenBaseKeySource(String uid)
    {
        String source = String.format("%s+%s#%s", TimeHelper.getCurrentTimestamp().toString(),
                getRandomUUIDWithoutDash());
        return source;
    }

    public static String generateToken(String keySource)
    {
        byte[] encodeKeyBytes = sEncryptor.encryptData(keySource);
        String encodeToken = null;
        try
        {
            encodeToken = new String(encodeKeyBytes, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            CommonServiceLog.e(TAG, "UnsupportedEncodingException for UTF-8", e);
        }
        return encodeToken;
    }

    public static String decodeToken(String token)
    {
        try
        {
            byte[] tokenBytes = token.getBytes("UTF-8");
            return sEncryptor.decryptData(tokenBytes);

        } catch (UnsupportedEncodingException e1)
        {
            CommonServiceLog.e(TAG, "UnsupportedEncodingException for UTF-8", e1);
        }
        return null;
    }

    private static String getRandomUUIDWithoutDash()
    {
        String rawUUID = getRandomUUID();
        return rawUUID.replace("-", "");
    }

    private static String getRandomUUID()
    {
        return UUID.randomUUID().toString();
    }
}
