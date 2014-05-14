package com.logpie.customer.tool;

import org.json.JSONObject;

import com.logpie.cutomer.logic.CustomerManager.CustomerRequestType;

public abstract class CustomerCallback
{
    abstract void onSuccess(CustomerRequestType type, JSONObject data);

    abstract void onError(CustomerRequestType type, CustomerErrorType errorType, String errorMessage);
}
