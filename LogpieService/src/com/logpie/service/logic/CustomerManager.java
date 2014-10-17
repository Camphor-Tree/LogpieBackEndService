package com.logpie.service.logic;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.RequestKeys;
import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.authentication.AuthRequestHandler;
import com.logpie.service.data.CustomerDataManager;
import com.logpie.service.data.DataCallback;
import com.logpie.service.data.DataManager;
import com.logpie.service.error.ErrorType;
import com.logpie.service.error.HttpRequestIsNullException;
import com.logpie.service.logic.helper.ManagerHelper;
import com.logpie.service.logic.helper.ManagerHelper.RequestType;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.HttpRequestParser;
import com.logpie.service.util.JSONHelper;
import com.logpie.service.util.ServiceLog;

/**
 * Customer Manager class for handling customer type request
 * 
 * @author xujiahang
 */
public class CustomerManager
{
    private static String TAG = CustomerManager.class.getName();
    private static CustomerManager sCustomerManager;
    private static ServletContext sGlobalUniqueContext;
    private static DataManager sCustomerDataManager;

    /**
     * CustomerManager is singleton
     */
    private CustomerManager()
    {

    }

    public synchronized static CustomerManager getInstance()
    {
        if (sCustomerManager == null)
        {
            sCustomerManager = new CustomerManager();
        }
        return sCustomerManager;
    }

    public static void initialize(ServletContext globalUniqueContext)
    {
        sGlobalUniqueContext = globalUniqueContext;
        sCustomerDataManager = CustomerDataManager.getInstance();
    }

    /**
     * Main function to handle customer type request
     * 
     * @param request
     * @param response
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    {
        AuthRequestHandler authHandler = new AuthRequestHandler();
        boolean authSuccess = authHandler.handleAuthentication(request, response, "LogpieService");
        if (!authSuccess)
        {
            return;
        }

        JSONObject postData = null;
        try
        {
            postData = HttpRequestParser.httpRequestParser(request);
            ServiceLog.d(TAG, "Received postData:" + postData);
        } catch (HttpRequestIsNullException e)
        {
            ServiceLog.e(TAG, "HttpRequestParser is null when parsing a customer service request.",
                    e);
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
                return;
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

    /**
     * Handle INSERT type request
     * 
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleInsert(JSONObject postData, String service,
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
            case RequestKeys.SERVICE_REGISTER:
                sql = register(postData);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sCustomerDataManager.executeInsert(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT,
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
                    "JSONException happened when making an INSERT operation of customer service.",
                    e);
        }
    }

    /**
     * Handle QUERY type request
     * 
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleQuery(JSONObject postData, String service,
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
            case RequestKeys.SERVICE_SHOW_PROFILE:
                sql = queryUserProfile(postData);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sCustomerDataManager.executeQuery(returnSet, sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT,
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
                        ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_SERVER_ERROR_MESSAGE)));
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
                    "JSONException happened when making a QUERY operation of customer service.", e);
        }
    }

    /**
     * Handle UPDATE type request
     * 
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleUpdate(JSONObject postData, String service,
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
            case RequestKeys.SERVICE_EDIT_PROFILE:
                sql = editUserProfile(postData);
                break;
            default:
                ServiceLog.e(requestID, "Unsupported request service type.");
                ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
                return;
            }

            sCustomerDataManager.executeUpdate(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT,
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
                    "JSONException happened when making an UPDATE operation of customer service.",
                    e);
        }
    }

    private String register(JSONObject postData) throws JSONException
    {
        // Build the set of required keys
        ArrayList<String> keySet = new ArrayList<String>();
        keySet.add(RequestKeys.KEY_EMAIL);
        keySet.add(RequestKeys.KEY_NICKNAME);
        keySet.add(RequestKeys.KEY_CITY);

        String sql = JSONHelper.parseToSQL(postData, keySet, DatabaseSchema.SCHEMA_TABLE_USER,
                RequestKeys.REQUEST_TYPE_INSERT, null);

        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "Failed to build SQL when registering a user.");
        }
        else
        {
            ServiceLog.d(TAG, "Built the SQL to register a user: " + sql);
        }

        return sql;
    }

    private String queryUserProfile(JSONObject postData) throws JSONException
    {
        ArrayList<String> returnSet = new ArrayList<String>();

        String sql = JSONHelper.parseToSQL(postData, null, DatabaseSchema.SCHEMA_TABLE_USER,
                RequestKeys.REQUEST_TYPE_QUERY, returnSet);

        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "Failed to build SQL when getting a user profile.");
        }
        else
        {
            ServiceLog.d(TAG, "Built the SQL to get a user profile: " + sql);
        }

        return sql;
    }

    private String editUserProfile(JSONObject postData) throws JSONException
    {
        String sql = JSONHelper.parseToSQL(postData, null, DatabaseSchema.SCHEMA_TABLE_USER,
                RequestKeys.REQUEST_TYPE_UPDATE, null);

        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "Failed to build SQL when editting a user profile.");
        }
        else
        {
            ServiceLog.d(TAG, "Built the SQL to edit a user profile: " + sql);
        }

        return sql;
    }
}
