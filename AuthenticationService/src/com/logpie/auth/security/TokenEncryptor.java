package com.logpie.auth.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.logpie.service.util.ServiceLog;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class TokenEncryptor extends AbstractDataEncryptor
{
    private static final String TAG = TokenEncryptor.class.getName();
    private static final String sAlgorithm = "PBKDF2WithHmacSHA1";
    private static final int sEncryptionKeyLength = 128;
    private static final int sIterationNum = 1000;

    private static final String sLogpieSecretPassword = "logpie";
    private static final String sSalt = "logpieInAquarium";

    public TokenEncryptor()
    {
        super();
    }

    @Override
    public String getEncryptionKey()
    {
        try
        {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance(sAlgorithm);
            // use device secret to encrypt data
            final KeySpec keySpec = new PBEKeySpec(sLogpieSecretPassword.toCharArray(),
                    sSalt.getBytes(), sIterationNum, sEncryptionKeyLength);
            final SecretKey key = factory.generateSecret(keySpec);
            return Base64.encode(key.getEncoded());
        } catch (NoSuchAlgorithmException e)
        {
            ServiceLog.e(TAG, "Doesn't support such algorithm", e);
        } catch (InvalidKeySpecException e)
        {
            ServiceLog.e(TAG, "InvalidKeySpecException", e);
        }
        return null;
    }
}
