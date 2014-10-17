package com.logpie.auth.logic;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.EndPoint.ServiceURL;
import com.logpie.commonlib.RequestKeys;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.util.JSONHelper;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

public final class RegisterHelper
{
    private static final String TAG = RegisterHelper.class.getName();

    public static void callCustomerServiceToRegister(final String uid, final String email,
            final String nickName, final String city, final String request_id)
    {
        JSONObject requestJSON = buildCustomerServiceJSON(uid, email, nickName, city, request_id);
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

    private static JSONObject buildCustomerServiceJSON(String uid, String email, String nickName,
            String city, String request_id)
    {
        JSONObject requestJSON = new JSONObject();
        try
        {

            requestJSON.put(RequestKeys.KEY_REQUEST_ID, request_id);
            requestJSON.put(RequestKeys.KEY_REQUEST_SERVICE, RequestKeys.SERVICE_REGISTER);
            requestJSON.put(RequestKeys.KEY_REQUEST_TYPE, RequestKeys.REQUEST_TYPE_INSERT);

            Map<String, String> insertKeyValueMap = new HashMap<String, String>();
            insertKeyValueMap.put(RequestKeys.KEY_UID, uid);
            insertKeyValueMap.put(RequestKeys.KEY_EMAIL, email);
            insertKeyValueMap.put(RequestKeys.KEY_NICKNAME, nickName);
            insertKeyValueMap.put(RequestKeys.KEY_CITY, city);

            JSONArray insertKeyvaluePair = JSONHelper.buildInsertKeyValue(insertKeyValueMap);
            requestJSON.put(RequestKeys.KEY_INSERT_KEYVALUE_PAIR, insertKeyvaluePair);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG,
                    "JSONException when build the register request JSON to CustomerService");
            e.printStackTrace();
            return null;
        }
        return requestJSON;

    }
}
