package com.logpie.cutomer.logic;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.customer.data.CustomerDataManager;
import com.logpie.customer.tool.CustomerErrorType;
import com.logpie.customer.tool.CustomerServiceLog;
import com.logpie.customer.tool.HttpRequestParser;

public class CustomerManager
{
    public static enum CustomerRequestType
    {
        ADD, FIND, UPDATE, DELETE;
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

    public void handleAuthenticationRequest(HttpServletRequest request, HttpServletResponse response)
    {
        JSONObject postBody = HttpRequestParser.httpRequestParser(request);
        if (postBody != null)
        {
            CustomerRequestType type = getCustomerType(postBody);
            switch (type)
            {
            case ADD:
                handleAdd(postBody, response);
                break;
            case FIND:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            default:
            {
                CustomerServiceLog.e(TAG, "Unsupported Type!");
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
            CustomerServiceLog.e(
                    TAG,
                    "Returning error code when handling authentication ->" + "ErrorCode:"
                            + errorType.getErrorCode() + " ErrorMessage:"
                            + errorType.getErrorMessage());
        } catch (IOException e)
        {
            CustomerServiceLog.e(TAG, "IOException happend when sendErrorCode");
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
                CustomerServiceLog.e(TAG, "JSONException happend when parsing Authentication Type");
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    private void handleAdd(JSONObject postBody, HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }
}
