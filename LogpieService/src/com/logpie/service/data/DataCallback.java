package com.logpie.service.data;

import org.json.JSONObject;

public interface DataCallback {
	
	abstract void onSuccess(JSONObject result);
    
	abstract void onError(JSONObject error);
	
}
