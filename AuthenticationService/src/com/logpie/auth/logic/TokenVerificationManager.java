package com.logpie.auth.logic;

import java.sql.Timestamp;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.util.ResponseKeys;
import com.logpie.service.util.ServiceLog;

public class TokenVerificationManager
{
    public static final String sFailReasonTokenExpiration = "token_expire";
    public static final String sFailReasonTokenInvalid = "token_invalid";
    public static final String sFailReasonTokenNoScope = "no_scope";

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

    private void decomposeToken()
    {
        if (mIsDecomposed == false)
        {
            int uidOffset = mPlainToken.indexOf("+");
            int randomUIDOffset = mPlainToken.indexOf("$");
            int timeStampOffset = mPlainToken.indexOf("#");

            String timeStampString = mPlainToken
                    .substring(uidOffset + 1, timeStampOffset);
            mTokenGeneratedTime = Timestamp.valueOf(timeStampString);

            mUidInToken = mPlainToken.substring(0, uidOffset);
            String scopeString = mPlainToken.substring(randomUIDOffset,
                    mPlainToken.length());
            String[] scopes = scopeString.split("$");
            for (String scope : scopes)
            {
                mScopes.add(scope);
            }
            mIsDecomposed = true;
        }
    }

    public boolean checkTokenExpiration()
    {
        long tokenExpirationTimeMillis = 0;
        if (mTokenType.equals("access_token"))
        {
            tokenExpirationTimeMillis = mTokenGeneratedTime.getTime()
                    + sAccessTokenExpiration;
        }
        else if (mTokenType.equals("refresh_token"))
        {
            tokenExpirationTimeMillis = mTokenGeneratedTime.getTime()
                    + sRefreshTokenExpiration;
        }
        else
        {
            ServiceLog.e(TAG, "The token_type cannot be recognized! Token_type is:"
                    + mTokenType);
            return false;
        }

        if (System.currentTimeMillis() < tokenExpirationTimeMillis)
        {
            return true;
        }
        else
        {
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

    public JSONObject buildSuccessJSON()
    {
        JSONObject successJSON = new JSONObject();
        try
        {
            successJSON.put(ResponseKeys.KEY_AUTHENTICATION_RESULT, "success");
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException when build success result", mRequestId, e);
            return null;
        }
        return successJSON;
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
