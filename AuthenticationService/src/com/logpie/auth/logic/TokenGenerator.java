package com.logpie.auth.logic;

import java.util.UUID;

import com.logpie.auth.security.AbstractDataEncryptor;
import com.logpie.auth.security.TokenEncryptor;
import com.logpie.service.util.TimeHelper;

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
        String source = String.format("%s+%s#%s", uid, TimeHelper.getCurrentTimestamp()
                .toString(), getRandomUUIDWithoutDash());
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
        String source = String.format("%s+%s#%s", uid, TimeHelper.getCurrentTimestamp()
                .toString(), getRandomUUIDWithoutDash());
        return source;
    }

    public static String generateToken(String keySource)
    {
        String encodeToken = null;

        encodeToken = sEncryptor.encryptData(keySource);

        return encodeToken;
    }

    public static String decodeToken(String token)
    {
        return sEncryptor.decryptData(token);
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
