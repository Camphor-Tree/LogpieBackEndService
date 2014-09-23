package com.logpie.auth.logic;

import org.json.JSONObject;

import com.logpie.auth.logic.AuthenticationManager.AuthenticationType;
import com.logpie.service.error.ErrorType;

public abstract class AuthCallback
{
    abstract void onSuccess(AuthenticationType type, JSONObject data);

    abstract void onError(AuthenticationType type, ErrorType errorType,
            String errorMessage);
}
