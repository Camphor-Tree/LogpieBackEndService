package com.logpie.auth.logic;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.connection.EndPoint.ServiceURL;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.util.JSONHelper;
import com.logpie.service.util.RequestKeys;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

public final class RegisterHelper
{
    private static final String TAG = RegisterHelper.class.getName();

    public static void callCustomerServiceToRegister(final String uid,
            final String email, final String nickName, final String city,
            final String request_id)
    {
        JSONObject requestJSON = buildCustomerServiceJSON(uid, email, nickName, city,
                request_id);
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
            String nickName, String city, String request_id)
    {
        JSONObject requestJSON = new JSONObject();
        try
        {
            requestJSON.put(RequestKeys.KEY_REQUEST_ID, request_id);
            requestJSON
                    .put(RequestKeys.KEY_REQUEST_SERVICE, RequestKeys.SERVICE_REGISTER);
            requestJSON
                    .put(RequestKeys.KEY_REQUEST_TYPE, RequestKeys.REQUEST_TYPE_INSERT);

            ArrayList<String> column = new ArrayList<String>();
            column.add(RequestKeys.KEY_UID);
            column.add(RequestKeys.KEY_EMAIL);
            column.add(RequestKeys.KEY_NICKNAME);
            column.add(RequestKeys.KEY_CITY);

            ArrayList<String> value = new ArrayList<String>();
            value.add(uid);
            value.add(email);
            value.add(nickName);
            value.add(city);

            JSONArray insertKeyvaluePair = JSONHelper.buildInsertKeyValue(column, value);
            requestJSON.put(RequestKeys.KEY_INSERT_KEYVALUE_PAIR, insertKeyvaluePair);
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
