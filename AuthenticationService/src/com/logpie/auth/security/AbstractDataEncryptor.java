package com.logpie.auth.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.logpie.service.util.ServiceLog;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public abstract class AbstractDataEncryptor
{
    private final static String TAG = AbstractDataEncryptor.class.getName();
    private final static int INITIALIZATION_VECTOR_LENGTH = 16;
    private final static String AES_MODE = "AES/CBC/PKCS5Padding";
    private final static String UTF8 = "UTF-8";
    private byte[] mEncryptionKey;
    private SecretKeySpec mKeySpec;

    protected Cipher mCipher;

    protected AbstractDataEncryptor()
    {
        try
        {
            mEncryptionKey = Base64.decode(getEncryptionKey());
        } catch (Base64DecodingException e1)
        {
            ServiceLog
                    .e(TAG, "No Such Algorithm Exception, AES/CBC is not supported", e1);
        }
        mKeySpec = new SecretKeySpec(mEncryptionKey, "AES");
        try
        {
            mCipher = Cipher.getInstance(AES_MODE);
        } catch (NoSuchAlgorithmException e)
        {
            ServiceLog.e(TAG, "No Such Algorithm Exception, AES/CBC is not supported", e);
        } catch (NoSuchPaddingException e)
        {
            ServiceLog.e(TAG, "No Such Padding Exception, AES/CBC is not supported");
        }
    }

    public abstract String getEncryptionKey();

    public String encryptData(final String data)
    {
        if (data == null)
        {
            return null;
        }
        byte[] dataBytes = null;
        try
        {
            dataBytes = data.getBytes(UTF8);
        } catch (UnsupportedEncodingException e1)
        {
            ServiceLog.e(TAG, "UnsupportedEncodingException", e1);
        }
        byte[] iv = generateIVForEncryption();
        try
        {
            if (mCipher == null)
            {
                ServiceLog.e(TAG, "Cipher is null!");
                return null;
            }
            mCipher.init(Cipher.ENCRYPT_MODE, mKeySpec, new IvParameterSpec(iv));
            byte[] cipherText = doCipherOperation(mCipher, dataBytes, 0, dataBytes.length);
            // The final encryption result consists of iv and encryption data
            // Attach iv array in front of encryption byte array.
            return Base64.encode(concat(iv, cipherText), Base64.BASE64DEFAULTLENGTH);

        } catch (InvalidKeyException e)
        {
            ServiceLog.e(TAG, "InvalidKeyException", e);
        } catch (InvalidAlgorithmParameterException e)
        {
            ServiceLog.e(TAG, "InvalidAlgorithmParameterException", e);
        }
        return null;
    };

    public String decryptData(final String dataString)
    {
        if (dataString == null)
        {
            return null;
        }
        byte[] dataBytes = null;
        try
        {
            dataBytes = Base64.decode(dataString);
        } catch (Base64DecodingException e)
        {
            ServiceLog.e(TAG, "Base64 Decoding Exception", e);
        }
        if (dataBytes == null)
        {
            return null;
        }

        try
        {
            mCipher.init(Cipher.DECRYPT_MODE, mKeySpec, new IvParameterSpec(dataBytes, 0,
                    INITIALIZATION_VECTOR_LENGTH));
            byte[] plainDataBytes = doCipherOperation(mCipher, dataBytes,
                    INITIALIZATION_VECTOR_LENGTH, dataBytes.length
                            - INITIALIZATION_VECTOR_LENGTH);
            // The final encryption result consists of iv and encryption data
            // Attach iv array in front of encryption byte array.
            return new String(plainDataBytes, UTF8);
        } catch (InvalidKeyException e)
        {
            ServiceLog.e(TAG, "InvalidKeyException", e);
        } catch (InvalidAlgorithmParameterException e)
        {
            ServiceLog.e(TAG, "InvalidAlgorithmParameterException", e);
        } catch (UnsupportedEncodingException e)
        {
            ServiceLog.e(TAG, "Doesn't support UTF-8, which is impossible", e);
        }
        return null;
    };

    private byte[] doCipherOperation(Cipher cipher, byte[] dataToEncrypt, int offset,
            int length)
    {
        try
        {
            return cipher.doFinal(dataToEncrypt, offset, length);
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateIVForEncryption()
    {
        final SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[INITIALIZATION_VECTOR_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private byte[] concat(byte[] iv, byte[] encryptedDataBytes)
    {
        byte[] finalResult = new byte[iv.length + encryptedDataBytes.length];
        try
        {
            System.arraycopy(iv, 0, finalResult, 0, iv.length);
            System.arraycopy(encryptedDataBytes, 0, finalResult, iv.length,
                    encryptedDataBytes.length);
        } catch (Exception e)
        {
            ServiceLog.e(TAG, "Exception happens when try to concat two bytes array", e);
            return null;
        }
        return finalResult;
    }

}
