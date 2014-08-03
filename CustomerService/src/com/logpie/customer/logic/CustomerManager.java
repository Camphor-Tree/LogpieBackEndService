package com.logpie.customer.logic;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.customer.data.CustomerDataManager;
import com.logpie.customer.data.CustomerDataManager.DataCallback;
import com.logpie.customer.data.SQLHelper;
import com.logpie.customer.tool.CustomerErrorType;
import com.logpie.service.common.exception.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;

public class CustomerManager
{
    public static enum CustomerRequestType
    {
        INSERT, FIND, UPDATE, DELETE;
        public static CustomerRequestType matchType(String type)
        {
            for (CustomerRequestType requestType : CustomerRequestType.values())
            {
                if (requestType.toString().equals(type))
                    return requestType;
            }
            return null;
        }
    }

    private static String TAG = CustomerManager.class.getName();
    private static CustomerManager sCustomerManager;

    private static ServletContext sGlobalUniqueContext;
    private static CustomerDataManager sCustomerDataManager;
    

    public static void initialize(ServletContext globalUniqueContext)
    {
        sGlobalUniqueContext = globalUniqueContext;
        sCustomerDataManager = CustomerDataManager.getInstance();
    }

    public synchronized static CustomerManager getInstance()
    {
        if (sCustomerManager == null)
        {
            sCustomerManager = new CustomerManager();
            sCustomerDataManager = CustomerDataManager.getInstance();
        }
        return sCustomerManager;
    }

    private CustomerManager()
    {

    }

    public void handleCustomerRequest(HttpServletRequest request, HttpServletResponse response)
    {
        JSONObject postBody;
		try {
			postBody = HttpRequestParser.httpRequestParser(request);
		} catch (HttpRequestIsNullException e) {
			e.printStackTrace();
			CommonServiceLog.e(TAG, "HttpRequestParser is null");
			return;
		}
        if (postBody != null)
        {
            CustomerRequestType type = getCustomerType(postBody);
            switch (type)
            {
            case INSERT:
                handleInsert(postBody, response);
                break;
            case FIND:
            	handleFind(postBody, response);
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            default:
            {
                CommonServiceLog.e(TAG, "Unsupported Type!");
                handleCustomerResponseWithError(response, CustomerErrorType.BAD_REQUEST);
                break;
            }
            }
        }
        else
        {
            handleCustomerResponseWithError(response, CustomerErrorType.BAD_REQUEST);
        }
    }

    private void handleCustomerResponseWithError(HttpServletResponse response,
            CustomerErrorType errorType)
    {
        try
        {
            response.sendError(errorType.getErrorCode());
            CommonServiceLog.e(
                    TAG,
                    "Returning error code when handling customer ->" + "ErrorCode:"
                            + errorType.getErrorCode() + " ErrorMessage:"
                            + errorType.getErrorMessage());
        } catch (IOException e)
        {
            CommonServiceLog.e(TAG, "IOException happend when sendErrorCode");
        }
    }

    // TODO: modify
    private CustomerRequestType getCustomerType(JSONObject postData)
    {
        if (postData.has(CustomerRequestKeys.KEY_CUSTOMER_TYPE))
        {
            try
            {
                return CustomerRequestType.matchType(postData
                        .getString(CustomerRequestKeys.KEY_CUSTOMER_TYPE));
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException happend when parsing Customer Service Type");
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    private void handleInsert(JSONObject regData, final HttpServletResponse response)
    {
    	String requestID = null;
        try
        {
            // If exception happened, just generate a random requestID
            requestID = regData.getString(CustomerRequestKeys.KEY_REQUEST_ID) != null ? regData
                    .getString(CustomerRequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();
            CommonServiceLog.d(TAG, "Start handling insert: requestID=" + requestID);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            CommonServiceLog.e(TAG,
                    "JSONException when getting requestID, setting a new random requestID");
            CommonServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        try
        {
        	String uid = regData.getString(CustomerRequestKeys.KEY_UID);
            String email = regData.getString(CustomerRequestKeys.KEY_EMAIL);
            String nickname = regData.getString(CustomerRequestKeys.KEY_NICKNAME);
            
            // Generate the new tokens for all new users
            String sql = SQLHelper.buildInsertSQL(uid, email, nickname);
            sCustomerDataManager.executeInsert(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(CustomerResponseKeys.KEY_REQUEST_ID, request_id);
                        //handleAuthResult(true, result, response);
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
                        CommonServiceLog.e(TAG, "JSONException when getting insert result");
                        CommonServiceLog.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        
                    	handleCustomerResponseWithError(response, CustomerErrorType.valueOf(error
                                .getString(CustomerDataManager.KEY_CALLBACK_ERROR)));
                    } catch (JSONException e)
                    {
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            //handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG, "JSONException when getting email/password");
            CommonServiceLog.e(TAG, e.getMessage());
        }
    }
    
    private void handleFind(JSONObject regData, final HttpServletResponse response)
    {
    	String requestID = null;
        try
        {
            // If exception happened, just generate a random requestID
            requestID = regData.getString(CustomerRequestKeys.KEY_REQUEST_ID) != null ? regData
                    .getString(CustomerRequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();
            CommonServiceLog.d(TAG, "Start handling find: requestID=" + requestID);
        } catch (JSONException e)
        {
            requestID = UUID.randomUUID().toString();
            CommonServiceLog.e(TAG,
                    "JSONException when getting requestID, setting a new random requestID");
            CommonServiceLog.e(TAG, e.getMessage());
        }

        final String request_id = requestID;

        try
        {
        	String key = regData.getString(CustomerRequestKeys.KEY_KEYWORD);
            String value = regData.getString(CustomerRequestKeys.KEY_VALUE);
            
            // Generate the new tokens for all new users
            String sql = SQLHelper.buildFindSQL(key,value);
            sCustomerDataManager.executeFind(sql, new DataCallback()
            {

                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(CustomerResponseKeys.KEY_REQUEST_ID, request_id);
                        //handleAuthResult(true, result, response);
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
                        CommonServiceLog.e(TAG, "JSONException when getting insert result");
                        CommonServiceLog.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onError(JSONObject error)
                {
                    try
                    {
                        
                    	handleCustomerResponseWithError(response, CustomerErrorType.valueOf(error
                                .getString(CustomerDataManager.KEY_CALLBACK_ERROR)));
                    } catch (JSONException e)
                    {
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, request_id, e.getMessage());
            //handleAuthenticationResponseWithError(response, AuthErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG, "JSONException when getting email/password");
            CommonServiceLog.e(TAG, e.getMessage());
        }
    }
    
}
