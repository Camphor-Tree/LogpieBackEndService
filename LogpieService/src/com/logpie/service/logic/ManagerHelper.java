package com.logpie.service.logic;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.error.ErrorType;
import com.logpie.service.util.HttpResponseWriter;
import com.logpie.service.util.RequestKeys;
import com.logpie.service.util.ResponseKeys;
import com.logpie.service.util.ServiceLog;

/**
 * Helper class for different managers
 * 
 * @author xujiahang
 */
public class ManagerHelper
{

    /**
     * Defined four kind of service request type: INSERT & QUERT & UPDATE &
     * DELETE
     */
    public static enum RequestType
    {
        insert, query, update, delete;
        public static RequestType matchType(String type)
        {
            for (RequestType requestType : RequestType.values())
            {
                if (requestType.toString().equals(type))
                    return requestType;
            }
            return null;
        }
    }

    private static final String TAG = ManagerHelper.class.getName();

    /**
     * Parse JSONObject to get request type from each request
     * 
     * @param postData
     * @param requestType
     * @param requestID
     * @return RequestType
     */
    static RequestType getRequestType(JSONObject postData, String keyRequestType,
            String requestID)
    {
        if (postData.has(keyRequestType))
        {
            try
            {
                return RequestType.matchType(postData.getString(keyRequestType));
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException happend when parsing request type.",
                        requestID, e);
            }
        }
        return null;
    }

    /**
     * Parse JSONObject to get request ID from each request
     * 
     * @param postData
     * @return requestID
     */
    static String getRequestID(JSONObject postData)
    {
        if (postData.has(RequestKeys.KEY_REQUEST_ID))
        {
            try
            {
                return postData.getString(RequestKeys.KEY_REQUEST_ID) != null ? postData
                        .getString(RequestKeys.KEY_REQUEST_ID) : UUID.randomUUID()
                        .toString();
            } catch (JSONException e)
            {
                String requestID = UUID.randomUUID().toString();
                ServiceLog
                        .d(TAG,
                                "JOSNException happened when parsing request ID. Generating a random request ID: ",
                                requestID);
                return requestID;
            }
        }
        return null;
    }

    /**
     * Helper function for handling the response from data manager
     * 
     * @param success
     * @param responseType
     * @param data
     * @param response
     */
    static void handleResponse(boolean success, String responseType, JSONObject data,
            HttpServletResponse response)
    {
        ServiceLog.d(TAG,
                "Handling Logpie Service request... JSON data is: " + data.toString());
        try
        {
            if (success)
            {
                data.put(responseType, ResponseKeys.RESULT_SUCCESS);
            }
            else
            {
                data.put(responseType, ResponseKeys.RESULT_ERROR);
            }
        } catch (JSONException e)
        {
            ServiceLog
                    .e(TAG,
                            "JSONException happened when handle Logpie Service request result",
                            e);
        }
        HttpResponseWriter.reponseWithSuccess(responseType, data, response);
    }

    /**
     * Helper function for handling the response when error happened
     * 
     * @param response
     * @param errorType
     */
    static void handleResponseWithError(HttpServletResponse response, ErrorType errorType)
    {
        try
        {
            response.sendError(errorType.getErrorCode());
            ServiceLog.e(TAG, "Returning error code when handling the response ->"
                    + "ErrorCode:" + errorType.getErrorCode() + " ErrorMessage:"
                    + errorType.getErrorMessage());
        } catch (IOException e)
        {
            ServiceLog.e(TAG, "IOException happend when sendErrorCode.", e);
        }
    }
}
