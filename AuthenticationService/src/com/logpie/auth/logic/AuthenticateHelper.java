package com.logpie.auth.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.EndPoint.ServiceURL;
import com.logpie.commonlib.RequestKeys;
import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.util.JSONHelper;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

/**
 * Used to handle the logic to call LogpieService (to get user basic info) to
 * finish authentication.
 * 
 * @author yilei
 * 
 */
public class AuthenticateHelper
{
    private Map<String, String> mUserInfoMap;

    public AuthenticateHelper()
    {
        mUserInfoMap = new HashMap<String, String>();
    }

    private static final String TAG = AuthenticateHelper.class.getName();
    private static final List<String> requiredInfo = Arrays.asList(RequestKeys.KEY_NICKNAME,
            RequestKeys.KEY_CITY, RequestKeys.KEY_GENDER);

    public Map<String, String> callCustomerServiceToGetInfo(final String uid,
            final String access_token, final String request_id)
    {
        JSONObject requestJSON = buildCustomerServiceJSON(request_id);
        if (requestJSON == null)
        {
            ServiceLog.e(TAG, "Cannot build requestJSON to call LogpieService to get user info.",
                    request_id);
            return null;
        }
        GenericConnection connection = new GenericConnection();
        connection.initialize(ServiceURL.CustomerService);
        connection.setRequestData(requestJSON);
        connection.setHeader("uid", uid);
        connection.setHeader("access_token", access_token);
        connection.send(new ServiceCallback()
        {

            @Override
            public void onSuccess(JSONObject result)
            {
                ServiceLog.d(TAG, "Success:" + result.toString());
                JSONObject queryResult;
                try
                {
                    queryResult = result.getJSONObject(GenericConnection.KEY_RESPONSE_DATA);
                } catch (JSONException e)
                {
                    ServiceLog.e(TAG,
                            "JSONException happened when parse the result from GenericConnection",
                            request_id, e);
                    return;
                }
                if (queryResult == null)
                {
                    ServiceLog.e(TAG, "GenericConnection's response doesn't contain result key!",
                            request_id);
                    return;
                }
                // nickName, gender cannot be null. Gender will default to
                // true(Male) on server
                String nickName;
                String city = null;
                String gender;
                try
                {
                    nickName = queryResult.getString(ResponseKeys.KEY_NICKNAME);
                } catch (JSONException e)
                {
                    // nick name can never be null
                    ServiceLog.e(TAG, "JSONException when parse nickName", request_id, e);
                    return;
                }
                try
                {
                    gender = queryResult.getString(ResponseKeys.KEY_GENDER);
                } catch (JSONException e)
                {
                    ServiceLog.e(TAG, "JSONException when parse gender", request_id, e);
                    return;
                }
                try
                {
                    city = queryResult.getString(ResponseKeys.KEY_CITY);
                } catch (JSONException e)
                {
                    ServiceLog.e(TAG, "JSONException when parse city", request_id, e);
                }

                if (nickName != null)
                {
                    mUserInfoMap.put(ResponseKeys.KEY_NICKNAME, nickName);
                }
                if (gender != null)
                {
                    mUserInfoMap.put(ResponseKeys.KEY_GENDER, gender);
                }
                if (city != null)
                {
                    mUserInfoMap.put(ResponseKeys.KEY_CITY, city);
                }
            }

            @Override
            public void onError(JSONObject errorMessage)
            {
                ServiceLog.d(TAG, "Error:" + errorMessage.toString());
            }
        });
        return mUserInfoMap;
    }

    private JSONObject buildCustomerServiceJSON(final String request_id)
    {
        JSONObject requestJSON = new JSONObject();
        try
        {
            requestJSON.put(RequestKeys.KEY_REQUEST_ID, request_id);
            requestJSON.put(RequestKeys.KEY_REQUEST_SERVICE, RequestKeys.SERVICE_SHOW_PROFILE);
            requestJSON.put(RequestKeys.KEY_REQUEST_TYPE, RequestKeys.REQUEST_TYPE_QUERY);
            JSONArray queryKeyvaluePair = JSONHelper.buildQueryKey(requiredInfo);
            requestJSON.put(RequestKeys.KEY_QUERY_KEY, queryKeyvaluePair);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG,
                    "JSONException when build the authenticate request JSON to CustomerService",
                    request_id, e);
            return null;
        }
        return requestJSON;
    }

    public Map<String, String> getUserInfoMap()
    {
        return mUserInfoMap;
    }
}
