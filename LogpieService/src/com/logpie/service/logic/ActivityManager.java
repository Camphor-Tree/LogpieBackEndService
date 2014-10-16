package com.logpie.service.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.RequestKeys;
import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.data.ActivityDataManager;
import com.logpie.service.data.DataCallback;
import com.logpie.service.data.DataManager;
import com.logpie.service.error.ErrorType;
import com.logpie.service.error.HttpRequestIsNullException;
import com.logpie.service.logic.ManagerHelper.RequestType;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.HttpRequestParser;
import com.logpie.service.util.JSONHelper;
import com.logpie.service.util.SQLHelper;
import com.logpie.service.util.ServiceLog;

public class ActivityManager
{
    private static final String TAG = ActivityManager.class.getName();
    private static final String DEFAULT_NUMBER = "25";

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
        JSONObject postData = null;
        try
        {
            postData = HttpRequestParser.httpRequestParser(request);
            ServiceLog.d(TAG, "Received postData:" + postData);
        } catch (HttpRequestIsNullException e)
        {
            ServiceLog.e(TAG,
                    "HttpRequestParser is null when parsing an activity service request.", e);
        }
        if (postData != null)
        {
            // requestID will never be null
            String requestID = ManagerHelper.getRequestID(postData);

            // TODO: verify token by auth service

            RequestType type = ManagerHelper.getRequestType(postData, RequestKeys.KEY_REQUEST_TYPE,
                    requestID);
            if (type == null)
            {
                ServiceLog.e(TAG, "Failed to find the request type from the request.", requestID);
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            }

            String service = null;
            if (postData.has(RequestKeys.KEY_REQUEST_SERVICE))
            {
                try
                {
                    service = postData.getString(RequestKeys.KEY_REQUEST_SERVICE);
                } catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    ServiceLog
                            .e(TAG,
                                    "JSONException happened when get the request service from the JSON data",
                                    requestID);
                }
            }
            else
            {
                ServiceLog.e(requestID, "Failed to find the request service key from the request.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            }

            switch (type)
            {
            case insert:
                handleInsert(postData, service, response, requestID);
                break;
            case query:
                handleQuery(postData, service, response, requestID);
                break;
            case update:
                handleUpdate(postData, service, response, requestID);
                break;
            case delete:
                break;
            default:
            {
                ServiceLog.e(TAG, "Unsupported type of activity service.", requestID);
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

    private void handleInsert(JSONObject postData, final String service,
            final HttpServletResponse response, final String requestID)
    {
        try
        {
            if (!postData.has(RequestKeys.KEY_INSERT_KEYVALUE_PAIR))
            {
                ServiceLog.e(TAG, "Failed to find the insert key value pair from the request.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            String sql = null;
            switch (service)
            {
            case RequestKeys.SERVICE_CREATE_ACTIVITY:
                sql = createActivity(postData);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sActivityDataManager.executeInsert(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_ACTIVITY_RESULT,
                                result, response);
                    } catch (JSONException e)
                    {
                        ServiceLog.logRequest(TAG, requestID, e.getMessage());
                        ServiceLog.e(TAG,
                                "JSONException happened when getting INSERT result successfully.",
                                requestID, e);
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_SERVER_ERROR_MESSAGE)));
                    } catch (JSONException e)
                    {
                        ServiceLog
                                .e(TAG,
                                        "JSONException happened when there is an error on getting INSERT result.",
                                        requestID, e);
                    }
                }
            });
        } catch (JSONException e)
        {
            ServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            ServiceLog.e(TAG,
                    "JSONException happened when making an INSERT operation of activity service.",
                    e);
        }
    }

    private void handleQuery(JSONObject postData, final String service,
            final HttpServletResponse response, final String requestID)
    {
        try
        {
            if (!postData.has(RequestKeys.KEY_QUERY_KEY))
            {
                ServiceLog.e(TAG, "Failed to find the query key from the request.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            String sql = null;
            ArrayList<String> returnSet = null;

            switch (service)
            {
            case RequestKeys.SERVICE_FIND_NEARBY_ACTIVITY:
                ActivityListQueryNearby queryNearby = new ActivityListQueryNearby();
                returnSet = queryNearby.getReturnSet(postData);
                sql = queryNearby.handleQuery(postData, requestID);
                break;
            case RequestKeys.SERVICE_FIND_ACTIVITY_BY_CITY:
                ActivityListQueryByCity queryByCity = new ActivityListQueryByCity();
                returnSet = queryByCity.getReturnSet(postData);
                sql = queryByCity.handleQuery(postData, requestID);
                break;
            case RequestKeys.SERVICE_FIND_ACTIVITY_BY_CATEGORY:
                ActivityListQueryByCategory queryByCategory = new ActivityListQueryByCategory();
                returnSet = queryByCategory.getReturnSet(postData);
                sql = queryByCategory.handleQuery(postData, requestID);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sActivityDataManager.executeQuery(returnSet, sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_ACTIVITY_RESULT,
                                result, response);
                    } catch (JSONException e)
                    {
                        ServiceLog.logRequest(TAG, requestID, e.getMessage());
                        ServiceLog.e(TAG,
                                "JSONException happened when getting QUERY result successfully.",
                                requestID, e);
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        if (error.has(ResponseKeys.KEY_SERVER_ERROR_MESSAGE))
                        {
                            String errorMessage = error
                                    .getString(ResponseKeys.KEY_SERVER_ERROR_MESSAGE);
                            ServiceLog.d(TAG, "Server Error Message:" + errorMessage);
                            ManagerHelper.handleResponseWithError(response,
                                    ErrorType.valueOf(errorMessage));
                        }
                        else
                        {
                            ServiceLog.e(TAG, "Cannot find the server error message key.");
                        }
                    } catch (JSONException e)
                    {
                        ServiceLog
                                .e(TAG,
                                        "JSONException happened when there is an error on getting QUERY result.",
                                        requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            ServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            ServiceLog.e(TAG,
                    "JSONException happened when making a QUERY operation of activity service.", e);
        }
    }

    private void handleUpdate(JSONObject postData, final String service,
            final HttpServletResponse response, final String requestID)
    {
        try
        {
            if (!postData.has(RequestKeys.KEY_UPDATE_KEYVALUE_PAIR))
            {
                ServiceLog.e(TAG, "Failed to find the update key value pair from the request.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            String sql = null;
            switch (service)
            {
            case RequestKeys.SERVICE_EDIT_ACTIVITY_DETAIL:
                sql = editActivity(postData);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sActivityDataManager.executeUpdate(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_ACTIVITY_RESULT,
                                result, response);
                    } catch (JSONException e)
                    {
                        ServiceLog.logRequest(TAG, requestID, e.getMessage());
                        ServiceLog.e(TAG,
                                "JSONException happened when getting UPDATE result successfully.",
                                requestID, e);
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_SERVER_ERROR_MESSAGE)));
                    } catch (JSONException e)
                    {
                        ServiceLog
                                .e(TAG,
                                        "JSONException happened when there is an error on getting UPDATE result.",
                                        requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            ServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            ServiceLog.e(TAG,
                    "JSONException happened making an UPDATE operation of activity service.", e);
        }
    }

    private String createActivity(JSONObject postData) throws JSONException
    {
        // Build the set of required keys
        ArrayList<String> keySet = new ArrayList<String>();
        keySet.add(RequestKeys.KEY_UID);
        keySet.add(RequestKeys.KEY_NICKNAME);
        keySet.add(RequestKeys.KEY_DESCRIPTION);
        keySet.add(RequestKeys.KEY_LOCATION);
        keySet.add(RequestKeys.KEY_START_TIME);
        keySet.add(RequestKeys.KEY_END_TIME);
        keySet.add(RequestKeys.KEY_CATEGORY);
        keySet.add(RequestKeys.KEY_CITY);

        JSONArray insertKeyvaluePair = postData.getJSONArray(RequestKeys.KEY_INSERT_KEYVALUE_PAIR);
        String lat = null;
        String lon = null;
        for (int i = 0; i < insertKeyvaluePair.length(); i++)
        {
            if (insertKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_INSERT_COLUMN))
            {
                if (insertKeyvaluePair.getJSONObject(i).getString(RequestKeys.KEY_INSERT_COLUMN)
                        .equals(RequestKeys.KEY_LATITUDE))
                {
                    lat = insertKeyvaluePair.getJSONObject(i).getString(
                            RequestKeys.KEY_INSERT_VALUE);
                    insertKeyvaluePair.remove(i);
                    i--;
                }
                else if (insertKeyvaluePair.getJSONObject(i)
                        .getString(RequestKeys.KEY_INSERT_COLUMN).equals(RequestKeys.KEY_LONGITUDE))
                {
                    lon = insertKeyvaluePair.getJSONObject(i).getString(
                            RequestKeys.KEY_INSERT_VALUE);
                    insertKeyvaluePair.remove(i);
                    i--;
                }
            }
        }
        if (lat == null || lon == null)
        {
            ServiceLog.e(TAG, "Cannot get the correct latitude and longitude.");
        }

        // Reset the latitude & longitude format for the database
        String latlon = "point(" + lat + "," + lon + ")";
        JSONObject object = new JSONObject();
        object.put(RequestKeys.KEY_INSERT_COLUMN, DatabaseSchema.SCHEMA_ACTIVITY_LATLON);
        object.put(RequestKeys.KEY_INSERT_VALUE, latlon);
        insertKeyvaluePair.put(object);

        String sql = JSONHelper.parseToSQL(postData, keySet, DatabaseSchema.SCHEMA_TABLE_ACTIVITY,
                RequestKeys.REQUEST_TYPE_INSERT, null);
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "Failed to build SQL when creating an activity.");
        }
        else
        {
            ServiceLog.d(TAG, "Built the SQL to create an antivity: " + sql);
        }

        return sql;
    }

    private String editActivity(JSONObject postData) throws JSONException
    {
        String sql = JSONHelper.parseToSQL(postData, null, DatabaseSchema.SCHEMA_TABLE_ACTIVITY,
                RequestKeys.REQUEST_TYPE_UPDATE, null);
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "Failed to build SQL when editting an activity.");
        }
        else
        {
            ServiceLog.d(TAG, "Built the SQL to edit an antivity: " + sql);
        }

        return sql;
    }

    private abstract class ActivityListQueryHandler
    {
        ArrayList<String> returnSet;

        final String handleQuery(JSONObject postData, String requestID) throws JSONException
        {
            returnSet = getReturnSet(postData);

            return buildQuerySQL(postData, returnSet, requestID);
        }

        ArrayList<String> getReturnSet(JSONObject postData) throws JSONException
        {

            JSONArray queryKey = postData.getJSONArray(RequestKeys.KEY_QUERY_KEY);

            if (queryKey == null)
            {
                ServiceLog
                        .d(TAG,
                                "There is no query key in the request that means select all keys from the table.");
                return null;
            }
            for (int i = 0; i < queryKey.length(); i++)
            {
                if (queryKey.getJSONObject(i).has(RequestKeys.KEY_QUERY_COLUMN))
                {
                    returnSet
                            .add(queryKey.getJSONObject(i).getString(RequestKeys.KEY_QUERY_COLUMN));
                }
            }

            return returnSet;
        }

        abstract String buildQuerySQL(JSONObject postData, ArrayList<String> returnSet,
                String requestID) throws JSONException;
    }

    private class ActivityListQueryNearby extends ActivityListQueryHandler
    {

        @Override
        String buildQuerySQL(JSONObject postData, ArrayList<String> returnSet, String requestID)
                throws JSONException
        {

            JSONArray constraintKeyvaluePair;
            if (!postData.has(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR))
            {
                ServiceLog.e(TAG,
                        "Failed to find the constraint key value pair from query request.",
                        requestID);
                return null;
            }

            constraintKeyvaluePair = postData
                    .getJSONArray(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR);

            String lat = null;
            String lon = null;

            for (int i = 0; i < constraintKeyvaluePair.length(); i++)
            {
                JSONObject data = constraintKeyvaluePair.getJSONObject(i);
                if (data.has(RequestKeys.KEY_CONSTRAINT_COLUMN))
                {
                    if (data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN).equals(
                            RequestKeys.KEY_LATITUDE))
                    {
                        lat = data.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
                        ServiceLog.d(TAG, "Parsed the latitude data is: " + lat);
                    }
                    if (data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN).equals(
                            RequestKeys.KEY_LONGITUDE))
                    {
                        lon = data.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
                        ServiceLog.d(TAG, "Parsed the longitude data is: " + lon);
                    }
                }
            }

            if (lat == null || lon == null)
            {
                ServiceLog.e(TAG, "Failed to find the lat/lon data from the query request.",
                        requestID);
                return null;
            }

            String number = null;
            if (postData.has(RequestKeys.KEY_LIMIT_NUMBER))
            {
                number = postData.getString(RequestKeys.KEY_LIMIT_NUMBER);
            }
            else
            {
                number = DEFAULT_NUMBER;
            }

            String sql = "select * from activity order by " + DatabaseSchema.SCHEMA_ACTIVITY_LATLON
                    + " <-> point(" + lat + ", " + lon + ") limit " + number + ";";
            return sql;
        }

    }

    private class ActivityListQueryByCity extends ActivityListQueryHandler
    {

        @Override
        String buildQuerySQL(JSONObject postData, ArrayList<String> returnSet, String requestID)
                throws JSONException
        {
            JSONArray constraintKeyvaluePair;
            if (!postData.has(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR))
            {
                ServiceLog.e(TAG,
                        "Failed to find the constraint key value pair from query request.",
                        requestID);
                return null;
            }

            constraintKeyvaluePair = postData
                    .getJSONArray(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR);
            String city = null;

            for (int i = 0; i < constraintKeyvaluePair.length(); i++)
            {
                JSONObject data = constraintKeyvaluePair.getJSONObject(i);
                if (data.has(RequestKeys.KEY_CONSTRAINT_COLUMN))
                {
                    if (data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN).equals(
                            RequestKeys.KEY_CITY))
                    {
                        city = data.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
                        ServiceLog.d(TAG, "Parsed the city data is: " + city);
                        break;
                    }
                }
            }

            if (city == null)
            {
                ServiceLog
                        .e(TAG, "Failed to find the city data from the query request.", requestID);
                return null;
            }

            // For constraint key & operator & value
            Map<String, Map<String, String>> constraints = new HashMap<String, Map<String, String>>();
            // For constraint operator & value
            Map<String, String> map = new HashMap<String, String>();
            map.put(RequestKeys.KEY_EQUAL, city);
            constraints.put(DatabaseSchema.SCHEMA_ACTIVITY_CITY, map);

            String number = null;
            if (postData.has(RequestKeys.KEY_LIMIT_NUMBER))
            {
                number = postData.getString(RequestKeys.KEY_LIMIT_NUMBER);
            }
            else
            {
                number = DEFAULT_NUMBER;
            }

            String orderBy = DatabaseSchema.SCHEMA_ACTIVITY_AID;
            boolean isASC = false;

            return SQLHelper.buildQuerySQL(DatabaseSchema.SCHEMA_TABLE_ACTIVITY, returnSet,
                    constraints, number, orderBy, isASC);

        }
    }

    private class ActivityListQueryByCategory extends ActivityListQueryHandler
    {

        @Override
        String buildQuerySQL(JSONObject postData, ArrayList<String> returnSet, String requestID)
                throws JSONException
        {
            JSONArray constraintKeyvaluePair;
            if (!postData.has(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR))
            {
                ServiceLog.e(TAG,
                        "Failed to find the constraint key value pair from query request.",
                        requestID);
                return null;
            }

            constraintKeyvaluePair = postData
                    .getJSONArray(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR);
            String category = null;
            String subCategory = null;

            for (int i = 0; i < constraintKeyvaluePair.length(); i++)
            {
                JSONObject data = constraintKeyvaluePair.getJSONObject(i);
                if (data.has(RequestKeys.KEY_CONSTRAINT_COLUMN))
                {
                    if (data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN).equals(
                            RequestKeys.KEY_CATEGORY))
                    {
                        category = data.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
                        ServiceLog.d(TAG, "Parsed the category data is: " + category);
                    }
                    else if (data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN).equals(
                            RequestKeys.KEY_SUBCATEGORY))
                    {
                        subCategory = data.getString(RequestKeys.KEY_CONSTRAINT_VALUE);
                        ServiceLog.d(TAG, "Parsed the subcategory data is: " + subCategory);
                    }
                }
            }

            // Category must be existed
            if (category == null)
            {
                ServiceLog
                        .e(TAG, "Failed to find category data from the query request.", requestID);
                return null;
            }

            // For constraint key & operator & value
            Map<String, Map<String, String>> constraints = new HashMap<String, Map<String, String>>();
            // For constraint operator & value
            Map<String, String> map_c = new HashMap<String, String>();
            map_c.put(RequestKeys.KEY_EQUAL, category);
            constraints.put(DatabaseSchema.SCHEMA_ACTIVITY_CATEGORY, map_c);

            if (subCategory != null)
            {
                Map<String, String> map_s = new HashMap<String, String>();
                map_s.put(RequestKeys.KEY_EQUAL, subCategory);
                constraints.put(DatabaseSchema.SCHEMA_ACTIVITY_SUBCATEGORY, map_s);
            }

            String number = null;
            if (postData.has(RequestKeys.KEY_LIMIT_NUMBER))
            {
                number = postData.getString(RequestKeys.KEY_LIMIT_NUMBER);
            }
            else
            {
                number = DEFAULT_NUMBER;
            }

            String orderBy = DatabaseSchema.SCHEMA_ACTIVITY_AID;
            boolean isASC = false;

            return SQLHelper.buildQuerySQL(DatabaseSchema.SCHEMA_TABLE_ACTIVITY, returnSet,
                    constraints, number, orderBy, isASC);
        }

    }
}
