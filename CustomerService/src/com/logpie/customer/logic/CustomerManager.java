package com.logpie.customer.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.customer.data.CustomerDataManager;
import com.logpie.customer.data.CustomerDataManager.DataCallback;
import com.logpie.customer.data.SQLHelper;
import com.logpie.customer.tool.CustomerErrorType;
import com.logpie.service.common.exception.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;
import com.logpie.service.common.helper.HttpResponseWriter;

public class CustomerManager
{
	/**
	 * Defined four kinds of customer service request type:
	 * INSERT & QUERT & UPDATE & DELETE
	 */
    public static enum CustomerRequestType
    {
        INSERT, QUERY, UPDATE, DELETE;
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
    
    private String getRequestID(JSONObject postData)
    {
    	if(postData.has(CustomerRequestKeys.KEY_REQUEST_ID))
    	{
    		try {
    			return postData.getString(CustomerRequestKeys.KEY_REQUEST_ID) != null ? postData
                        .getString(CustomerRequestKeys.KEY_REQUEST_ID) : UUID.randomUUID().toString();          
			} catch (JSONException e) 
			{
				String requestID = UUID.randomUUID().toString();
	            CommonServiceLog.d(TAG, "JOSNException happened when parsing request ID. Generating a random request ID: ", requestID);
				return requestID;
			}
    	}
    	return null;
    }
    
    private CustomerRequestType getCustomerType(JSONObject postData, String requestID)
    {
        if (postData.has(CustomerRequestKeys.KEY_CUSTOMER_TYPE))
        {
            try
            {
                return CustomerRequestType.matchType(postData
                        .getString(CustomerRequestKeys.KEY_CUSTOMER_TYPE));
            } catch (JSONException e)
            {
                CommonServiceLog.e(TAG, "JSONException happend when parsing Customer Service type.", requestID, e);
                return null;
            }
        }
        return null;
        
    }
    
    public void handleCustomerRequest(HttpServletRequest request, HttpServletResponse response)
    {
        JSONObject postBody = null;
		try {
			postBody = HttpRequestParser.httpRequestParser(request);
		} catch (HttpRequestIsNullException e) {
			CommonServiceLog.e(TAG, "HttpRequestParser is null when parsing a customer service request.", e);
			return;
		}
        if (postBody != null)
        {
        	String requestID = getRequestID(postBody);
            CustomerRequestType type = getCustomerType(postBody, requestID);
            
            switch (type)
            {
            case INSERT:
                handleInsert(postBody, response, requestID);
                break;
            case QUERY:
            	handleQuery(postBody, response, requestID);
                break;
            case DELETE:
                break;
            case UPDATE:
            	handleUpdate(postBody, response, requestID);
                break;
            default:
            {
                CommonServiceLog.e(TAG, "Unsupported type of Customer Service.", requestID);
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


    private void handleInsert(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
    	try
        {
        	String uid = postData.getString(CustomerRequestKeys.KEY_UID);
            String email = postData.getString(CustomerRequestKeys.KEY_EMAIL);
            String nickname = postData.getString(CustomerRequestKeys.KEY_NICKNAME);
            String city = postData.getString(CustomerRequestKeys.KEY_CITY);
            CommonServiceLog.d(TAG, "Parsed the INSERT request. uid: " + uid + ", email: " + email + ", nickname: " + nickname + ", city: ");
            
            // Generate the sql for register a user
            String sql = SQLHelper.buildRegisterSQL(uid, email, nickname, city);
            CommonServiceLog.d(TAG, "Built the SQL to register: "+sql);
            
            sCustomerDataManager.executeInsert(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(CustomerResponseKeys.KEY_REQUEST_ID, requestID);
                        result.put(CustomerResponseKeys.KEY_CUSTOMER_TYPE, CustomerRequestType.INSERT);
                        handleCustomerResult(true, result, response);
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                        CommonServiceLog.e(TAG, "JSONException happened when getting INSERT result successfully.", requestID, e);
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
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting INSERT result.", requestID, e);
                    }
                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            handleCustomerResponseWithError(response, CustomerErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG, "JSONException happened when getting uid/email/nickname before INSERT operation.", e);
        }
    }
    
    private void handleQuery(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
        try
        {
        	JSONArray requestArray = postData.getJSONArray(CustomerRequestKeys.KEY_QUERY_DATA);
        	ArrayList<String> keySet = new ArrayList<String>();
        	if(requestArray!=null)
        	{
        		for(int i=0;i<requestArray.length();i++)
        		{
        			JSONObject object = requestArray.getJSONObject(i);
        			String key = object.getString(CustomerRequestKeys.KEY_KEYWORD);        		
        			keySet.add(key);   		
        		}
        	}
        	String constraintKey = postData.getString(CustomerRequestKeys.KEY_CONSTRAINT_KEYWORD);
            String constraintValue = postData.getString(CustomerRequestKeys.KEY_CONSTRAINT_VALUE);
            CommonServiceLog.d(TAG, "Parsed the QUERY request.");
            
            // Generate the sql for query a record
            String sql = SQLHelper.buildQuerySQL(keySet, constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: "+sql);
            
            sCustomerDataManager.executeQuery(keySet, sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(CustomerResponseKeys.KEY_REQUEST_ID, requestID);
                        result.put(CustomerResponseKeys.KEY_CUSTOMER_TYPE, CustomerRequestType.QUERY);                        
                        handleCustomerResult(true, result, response);
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                        CommonServiceLog.e(TAG, "JSONException happened when getting QUERY result successfully.", requestID, e);
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
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting QUERY result.", requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            handleCustomerResponseWithError(response, CustomerErrorType.BAD_REQUEST);          
            CommonServiceLog.e(TAG, "JSONException happened when getting key/value before QUERY operation.", e);
        }              
    }
    
    private void handleUpdate(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
        try
        {
        	JSONArray requestArray = postData.getJSONArray(CustomerRequestKeys.KEY_KEYWORD);
        	ArrayList<String> keySet = new ArrayList<String>();
        	ArrayList<String> valueSet = new ArrayList<String>();
        	for(int i=0;i<requestArray.length();i++)
        	{
        		JSONObject object = requestArray.getJSONObject(i);
        		String key = object.getString(CustomerRequestKeys.KEY_KEYWORD);
        		String value = object.getString(CustomerRequestKeys.KEY_VALUE);
        		keySet.add(key);
        		valueSet.add(value);
        	}
        	String constraintKey = postData.getString(CustomerRequestKeys.KEY_CONSTRAINT_KEYWORD);
            String constraintValue = postData.getString(CustomerRequestKeys.KEY_CONSTRAINT_VALUE);
            CommonServiceLog.d(TAG, "Parsed the UPDATE request.");
            
            // Generate the sql for query a record
            String sql = SQLHelper.buildUpdateSQL(keySet, valueSet, constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: "+sql);
            
            sCustomerDataManager.executeUpdate(sql, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(CustomerResponseKeys.KEY_REQUEST_ID, requestID);
                        result.put(CustomerResponseKeys.KEY_CUSTOMER_TYPE, CustomerRequestType.UPDATE);                        
                        handleCustomerResult(true, result, response);
                    } catch (JSONException e)
                    {
                        CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
                        CommonServiceLog.e(TAG, "JSONException happened when getting UPDATE result successfully.", requestID, e);
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
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting UPDATE result.", requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            handleCustomerResponseWithError(response, CustomerErrorType.BAD_REQUEST);          
            CommonServiceLog.e(TAG, "JSONException happened when getting key/value before UPDATE operation.", e);
        }              
    }
    
    /**
     * Callback helper functions
     */
    private void handleCustomerResult(boolean success, JSONObject data,
			HttpServletResponse response) {
		CommonServiceLog.d(TAG, "Handling CustomerService request... JSON data is: " + data.toString());
    	try 
    	{
    		if (success)
    		{
				data.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_SUCCESS);
    		}else
    		{
    			data.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_ERROR);
    		}
    	}catch (JSONException e) {
			CommonServiceLog.e(TAG, "JSONException happened when handle CustomerService request result", e);
		}
        HttpResponseWriter.reponseWithSuccess(CustomerResponseKeys.KEY_CUSTOMER_RESULT, data, response);
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
            CommonServiceLog.e(TAG, "IOException happend when sendErrorCode.", e);
        }
    }
    
}
