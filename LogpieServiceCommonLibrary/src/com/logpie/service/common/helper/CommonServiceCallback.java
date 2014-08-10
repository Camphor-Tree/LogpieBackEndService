package com.logpie.service.common.helper;

import org.json.JSONObject;

public abstract class CommonServiceCallback {

    public abstract void onSuccess(JSONObject result);
    
    public abstract void onError(JSONObject errorMessage);
}
