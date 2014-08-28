package com.logpie.auth.tool;

import org.json.JSONObject;

import com.logpie.auth.logic.AuthenticationManager.AuthenticationType;

public abstract class AuthCallback
{
    abstract void onSuccess(AuthenticationType type, JSONObject data);

    abstract void onError(AuthenticationType type, AuthErrorType errorType, String errorMessage);
}
