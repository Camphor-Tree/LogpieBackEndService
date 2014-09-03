package com.logpie.service.logic;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.error.ErrorType;
import com.logpie.service.common.error.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;
import com.logpie.service.common.helper.RequestKeys;
import com.logpie.service.common.helper.ResponseKeys;
import com.logpie.service.common.helper.TimeHelper;
import com.logpie.service.data.ActivityDataManager;
import com.logpie.service.data.DataCallback;
import com.logpie.service.data.DataManager;
import com.logpie.service.data.DatabaseSchema;
import com.logpie.service.data.SQLHelper;
import com.logpie.service.logic.ManagerHelper.RequestType;

public class ActivityManager
{
    private static String TAG = ActivityManager.class.getName();
    private static ActivityManager sActivityManager;
    private static ServletContext sGlobalUniqueContext;
    private static DataManager sActivityDataManager;

    /**
     * CustomerManager is singleton
     */
    private ActivityManager()
    {

    }

    public synchronized static ActivityManager getInstance()
    {
        if (sActivityManager == null)
        {
            sActivityManager = new ActivityManager();
        }
        return sActivityManager;
    }

    public static void initialize(ServletContext globalUniqueContext)
    {
        sGlobalUniqueContext = globalUniqueContext;
        sActivityDataManager = ActivityDataManager.getInstance();
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    {
        JSONObject postBody = null;
        try
        {
            postBody = HttpRequestParser.httpRequestParser(request);
        } catch (HttpRequestIsNullException e)
        {
            CommonServiceLog.e(TAG,
                    "HttpRequestParser is null when parsing an activity service request.", e);
            return;
        }
        if (postBody != null)
        {
            String requestType = RequestKeys.KEY_ACTIVITY_TYPE;
            String requestID = ManagerHelper.getRequestID(postBody);

            // TODO: verify token by auth service

            RequestType type = ManagerHelper.getRequestType(postBody, requestType, requestID);
            switch (type)
            {
            case INSERT:
                handleInsert(postBody, response, requestID);
                break;
            case QUERY:
                handleQuery(postBody, response, requestID);
                break;
            case UPDATE:
                handleUpdate(postBody, response, requestID);
                break;
            case DELETE:
                break;
            default:
            {
                CommonServiceLog.e(TAG, "Unsupported type of Activity Service.", requestID);
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                break;
            }
            }
        }
        else
        {
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
        }
    }

    private void handleInsert(JSONObject postData, final HttpServletResponse response,
            final String requestID)
    {
        try
        {
            ArrayList<String> key_set = new ArrayList<String>();
            key_set.add(RequestKeys.KEY_UID);
            key_set.add(RequestKeys.KEY_DESCRIPTION);
            key_set.add(RequestKeys.KEY_LOCATION);
            key_set.add(RequestKeys.KEY_START_TIME);
            key_set.add(RequestKeys.KEY_END_TIME);
            key_set.add(RequestKeys.KEY_CATEGORY);
            key_set.add(RequestKeys.KEY_CITY);
            key_set.add(RequestKeys.KEY_LATITUDE);
            key_set.add(RequestKeys.KEY_LONGITUDE);

            ArrayList<String> value_set = new ArrayList<String>();
            value_set.add(postData.getString(RequestKeys.KEY_UID));
            value_set.add(postData.getString(RequestKeys.KEY_DESCRIPTION));
            value_set.add(postData.getString(RequestKeys.KEY_LOCATION));
            value_set.add(postData.getString(RequestKeys.KEY_CATEGORY));
            Timestamp startTime = TimeHelper.getTimestamp(postData
                    .getString(RequestKeys.KEY_START_TIME));
            Timestamp endTime = TimeHelper.getTimestamp(postData
                    .getString(RequestKeys.KEY_END_TIME));
            if (startTime == null || endTime == null)
            {
                CommonServiceLog.e(TAG,
                        "start time/end time is null when parsed the activity INSERT request",
                        requestID);
            }
            String cid = postData.getString(RequestKeys.KEY_CITY);
            String lat = postData.getString(RequestKeys.KEY_LATITUDE);
            String lon = postData.getString(RequestKeys.KEY_LONGITUDE);

            CommonServiceLog.d(TAG, "Parsed the INSERT request.");

            // Generate the sql for register a user
            String sql = SQLHelper.buildInsertSQL(DatabaseSchema.SCHEMA_TABLE_ACTIVITY, key_set,
                    value_set);
            CommonServiceLog.d(TAG, "Built the SQL to create an antivity: " + sql);

            sActivityDataManager.executeInsert(sql, RequestKeys.KEY_ACTIVITY_TYPE,
                    new DataCallback()
                    {
                        @Override
                        public void onSuccess(JSONObject result)
                        {
                            try
                            {
                                result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                                ManagerHelper.handleResponse(true,
                                        ResponseKeys.KEY_ACTIVITY_RESULT, result, response);
                            } catch (JSONException e)
                            {
                                CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when getting INSERT result successfully.",
                                                requestID, e);
                            }
                        }

                        @Override
                        public void onError(JSONObject error)
                        {
                            try
                            {
                                ManagerHelper.handleResponseWithError(response, ErrorType
                                        .valueOf(error.getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                            } catch (JSONException e)
                            {
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when there is an error on getting INSERT result.",
                                                requestID, e);
                            }
                        }
                    });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            CommonServiceLog
                    .e(TAG,
                            "JSONException happened when getting uid/email/nickname before INSERT operation.",
                            e);
        }
    }

    private void handleQuery(JSONObject postData, final HttpServletResponse response,
            final String requestID)
    {
        try
        {
            JSONArray requestArray = postData.getJSONArray(RequestKeys.KEY_QUERY);
            ArrayList<String> keySet = new ArrayList<String>();
            if (requestArray != null)
            {
                for (int i = 0; i < requestArray.length(); i++)
                {
                    JSONObject object = requestArray.getJSONObject(i);
                    String key = object.getString(RequestKeys.KEY_KEYWORD);
                    keySet.add(key);
                }
            }
            String constraintKey = postData.getString(RequestKeys.KEY_CONSTRAINT_KEYWORD);
            String constraintValue = postData.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
            CommonServiceLog.d(TAG, "Parsed the QUERY request.");

            // Generate the sql for query a record
            String sql = SQLHelper.buildQuerySQL(DatabaseSchema.SCHEMA_TABLE_ACTIVITY, keySet,
                    constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: " + sql);

            sActivityDataManager.executeQuery(keySet, sql, RequestKeys.KEY_CUSTOMER_TYPE,
                    new DataCallback()
                    {
                        @Override
                        public void onSuccess(JSONObject result)
                        {
                            try
                            {
                                result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                                ManagerHelper.handleResponse(true,
                                        ResponseKeys.KEY_CUSTOMER_RESULT, result, response);
                            } catch (JSONException e)
                            {
                                CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when getting QUERY result successfully.",
                                                requestID, e);
                            }
                        }

                        @Override
                        public void onError(JSONObject error)
                        {
                            try
                            {
                                ManagerHelper.handleResponseWithError(response, ErrorType
                                        .valueOf(error.getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                            } catch (JSONException e)
                            {
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when there is an error on getting QUERY result.",
                                                requestID, e);
                            }

                        }
                    });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG,
                    "JSONException happened when getting key/value before QUERY operation.", e);
        }
    }

    private void handleUpdate(JSONObject postData, final HttpServletResponse response,
            final String requestID)
    {
        try
        {
            JSONArray requestArray = postData.getJSONArray(RequestKeys.KEY_KEYWORD);
            ArrayList<String> keySet = new ArrayList<String>();
            ArrayList<String> valueSet = new ArrayList<String>();
            for (int i = 0; i < requestArray.length(); i++)
            {
                JSONObject object = requestArray.getJSONObject(i);
                String key = object.getString(RequestKeys.KEY_KEYWORD);
                String value = object.getString(RequestKeys.KEY_VALUE);
                keySet.add(key);
                valueSet.add(value);
            }
            String constraintKey = postData.getString(RequestKeys.KEY_CONSTRAINT_KEYWORD);
            String constraintValue = postData.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
            CommonServiceLog.d(TAG, "Parsed the UPDATE request.");

            // Generate the sql for query a record
            String sql = SQLHelper.buildUpdateSQL(DatabaseSchema.SCHEMA_TABLE_ACTIVITY, keySet,
                    valueSet, constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: " + sql);

            sActivityDataManager.executeUpdate(sql, RequestKeys.KEY_CUSTOMER_TYPE,
                    new DataCallback()
                    {
                        @Override
                        public void onSuccess(JSONObject result)
                        {
                            try
                            {
                                result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                                ManagerHelper.handleResponse(true,
                                        ResponseKeys.KEY_CUSTOMER_RESULT, result, response);
                            } catch (JSONException e)
                            {
                                CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when getting UPDATE result successfully.",
                                                requestID, e);
                            }
                        }

                        @Override
                        public void onError(JSONObject error)
                        {
                            try
                            {
                                ManagerHelper.handleResponseWithError(response, ErrorType
                                        .valueOf(error.getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                            } catch (JSONException e)
                            {
                                CommonServiceLog
                                        .e(TAG,
                                                "JSONException happened when there is an error on getting UPDATE result.",
                                                requestID, e);
                            }

                        }
                    });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG,
                    "JSONException happened when getting key/value before UPDATE operation.", e);
        }
    }

}
