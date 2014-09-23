package com.logpie.auth.logic;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.connection.EndPoint.ServiceURL;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

public final class RegisterHelper
{
    private static final String TAG = RegisterHelper.class.getName();

    public static void callCustomerServiceToRegister(String uid, String email,
            String nickName, String city)
    {
        JSONObject requestJSON = buildCustomerServiceJSON(uid, email, nickName, city);
        GenericConnection connection = new GenericConnection();
        connection.initialize(ServiceURL.CustomerService);
        connection.setRequestData(requestJSON);
        connection.send(new ServiceCallback()
        {

            @Override
            public void onSuccess(JSONObject result)
            {
                ServiceLog.d(TAG, "Success:" + result.toString());
            }

            @Override
            public void onError(JSONObject errorMessage)
            {
                ServiceLog.d(TAG, "Error:" + errorMessage.toString());
            }
        });
    }

    private static JSONObject buildCustomerServiceJSON(String uid, String email,
            String nickName, String city)
    {
        JSONObject requestJSON = new JSONObject();
        try
        {
            requestJSON.put("customer_service_type", "INSERT");
            requestJSON.put("uid", uid);
            requestJSON.put("email", email);
            requestJSON.put("nickname", nickName);
            requestJSON.put("city", city);
        } catch (JSONException e)
        {
            ServiceLog
                    .e(TAG,
                            "JSONException when build the register request JSON to CustomerService");
            e.printStackTrace();
            return null;
        }
        return requestJSON;

    }

}
