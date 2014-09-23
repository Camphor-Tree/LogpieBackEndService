package com.logpie.service.util;

import org.json.JSONObject;

public abstract class ServiceCallback {

    public abstract void onSuccess(JSONObject result);
    
    public abstract void onError(JSONObject errorMessage);
}
