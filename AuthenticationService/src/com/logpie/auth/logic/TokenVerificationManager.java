package com.logpie.auth.logic;

import java.sql.Timestamp;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.util.ServiceLog;

public class TokenVerificationManager
{
    public static final String TOKEN_TYPE_ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE_REFRESH_TOKEN = "refresh_token";

    public static final String sFailReasonTokenExpiration = "token_expire";
    public static final String sFailReasonTokenInvalid = "token_invalid";
    public static final String sFailReasonTokenNoScope = "no_scope";
    public static final String sFailReasonTokenUidNotMatch = "uid_does_not_match";

    private final String mDeclareUID;
    private final String mToken;
    private final String mTokenType;
    private final String mRequestId;
    private String mPlainToken;
    boolean mCanBeDecrypted;
    private String mUidInToken;
    private HashSet<String> mScopes;
    private boolean mIsDecomposed;
    private Timestamp mTokenGeneratedTime;

    private static final String TAG = TokenVerificationManager.class.getName();

    /**
     * Access token's expiration is 1 hour.
     */
    private static final long sAccessTokenExpiration = 1 * 3600 * 1000;

    /**
     * Refresh token's expiration is 1 year.
     */
    private static final long sRefreshTokenExpiration = 365 * 24 * 3600 * 1000;

    public TokenVerificationManager(String declare_uid, String token, String token_type,
            String requestId)
    {
        mDeclareUID = declare_uid;
        mToken = token;
        mTokenType = token_type;
        mRequestId = requestId;
    }

    public boolean verifyToken()
    {
        mPlainToken = TokenGenerator.decodeToken(mToken);
        if (mPlainToken == null || mPlainToken.equals(""))
        {
            mCanBeDecrypted = false;
            return false;
        }
        mCanBeDecrypted = true;
        return true;
    }

    public void decomposeToken()
    {
        if (mIsDecomposed == false)
        {
            int uidOffset = mPlainToken.indexOf("+");
            int randomUIDOffset = mPlainToken.indexOf("$");
            int timeStampOffset = mPlainToken.indexOf("#");

            String timeStampString = mPlainToken.substring(uidOffset + 1, timeStampOffset);
            mTokenGeneratedTime = Timestamp.valueOf(timeStampString);

            mUidInToken = mPlainToken.substring(0, uidOffset);
            String scopeString = mPlainToken.substring(randomUIDOffset, mPlainToken.length());
            String[] scopes = scopeString.split("$");
            if (mScopes == null)
            {
                mScopes = new HashSet<String>();
            }
            for (String scope : scopes)
            {
                mScopes.add(scope);
            }
            mIsDecomposed = true;
        }
    }

    /**
     * Check whether the token expires or not
     * 
     * @return
     * 
     *         true: token expired
     * 
     *         false: token still valid
     */
    public boolean checkTokenExpiration()
    {
        long tokenExpirationTimeMillis = 0;
        if (mTokenType.equals("access_token"))
        {
            tokenExpirationTimeMillis = mTokenGeneratedTime.getTime() + sAccessTokenExpiration;
        }
        else if (mTokenType.equals("refresh_token"))
        {
            tokenExpirationTimeMillis = mTokenGeneratedTime.getTime() + sRefreshTokenExpiration;
        }
        else
        {
            ServiceLog.e(TAG, "The token_type cannot be recognized! Token_type is:" + mTokenType);
            return false;
        }

        // If current time already bigger than expiration time, it means the
        // token is already invalid
        ServiceLog.d(TAG, "The tokenExpiration time is: " + tokenExpirationTimeMillis);
        ServiceLog.d(TAG, "The currentTimeMillis time is: " + System.currentTimeMillis());
        if (System.currentTimeMillis() > tokenExpirationTimeMillis)
        {
            ServiceLog.i(TAG, "Token already expired");
            return true;
        }
        else
        {
            ServiceLog.i(TAG, "Token has NOT expired");
            return false;
        }

    }

    public boolean rerifyTokenScope(TokenScopeManager.Scope scope)
    {
        if (mScopes.contains(scope.toString()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean verifyUID(String uid)
    {
        if (mUidInToken.equals(uid))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public JSONObject buildFailJSON(String reason)
    {
        JSONObject failJSON = new JSONObject();
        try
        {
            failJSON.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, "fail");
            failJSON.put(ResponseKeys.KEY_TOKEN_VALIDATION_FAIL_REASON, reason);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when build fail result", mRequestId, e);
            return null;
        }
        return failJSON;
    }

}
