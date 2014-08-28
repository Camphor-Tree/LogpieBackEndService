package com.logpie.service.logic;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.error.ErrorType;
import com.logpie.service.common.exception.HttpRequestIsNullException;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.HttpRequestParser;
import com.logpie.service.common.helper.RequestKeys;
import com.logpie.service.common.helper.ResponseKeys;
import com.logpie.service.data.CustomerDataManager;
import com.logpie.service.data.DataCallback;
import com.logpie.service.data.DataManager;
import com.logpie.service.data.SQLHelper;
import com.logpie.service.logic.ManagerHelper.RequestType;

/**
 * Customer Manager class for handling customer type request
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
     * @param request
     * @param response
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
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
        	String requestType = RequestKeys.KEY_CUSTOMER_TYPE;
        	String requestID = ManagerHelper.getRequestID(postBody);
        	
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
                CommonServiceLog.e(TAG, "Unsupported type of Customer Service.", requestID);
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
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleInsert(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
    	try
        {
        	String uid = postData.getString(RequestKeys.KEY_UID);
            String email = postData.getString(RequestKeys.KEY_EMAIL);
            String nickname = postData.getString(RequestKeys.KEY_NICKNAME);
            String city = postData.getString(RequestKeys.KEY_CITY);
            CommonServiceLog.d(TAG, "Parsed the INSERT request. uid: " + uid + ", email: " + email + ", nickname: " + nickname + ", city: ");
            
            // Generate the sql for register a user
            String sql = SQLHelper.buildRegisterSQL(uid, email, nickname, city);
            CommonServiceLog.d(TAG, "Built the SQL to register: "+sql);
            
            sCustomerDataManager.executeInsert(sql, RequestKeys.KEY_CUSTOMER_TYPE, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT, result, response);
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
                    	ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                    } catch (JSONException e)
                    {
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting INSERT result.", requestID, e);
                    }
                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);
            CommonServiceLog.e(TAG, "JSONException happened when getting uid/email/nickname before INSERT operation.", e);
        }
    }
    
    /**
     * Handle QUERY type request
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleQuery(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
        try
        {
        	JSONArray requestArray = postData.getJSONArray(RequestKeys.KEY_QUERY);
        	ArrayList<String> keySet = new ArrayList<String>();
        	if(requestArray!=null)
        	{
        		for(int i=0;i<requestArray.length();i++)
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
            String sql = SQLHelper.buildQuerySQL(keySet, constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: "+sql);
            
            sCustomerDataManager.executeQuery(keySet, sql, RequestKeys.KEY_CUSTOMER_TYPE, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT, result, response);
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
                    	ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                    } catch (JSONException e)
                    {
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting QUERY result.", requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);          
            CommonServiceLog.e(TAG, "JSONException happened when getting key/value before QUERY operation.", e);
        }              
    }
    
    /**
     * Handle UPDATE type request
     * @param postData
     * @param response
     * @param requestID
     */
    private void handleUpdate(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
        try
        {
        	JSONArray requestArray = postData.getJSONArray(RequestKeys.KEY_KEYWORD);
        	ArrayList<String> keySet = new ArrayList<String>();
        	ArrayList<String> valueSet = new ArrayList<String>();
        	for(int i=0;i<requestArray.length();i++)
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
            String sql = SQLHelper.buildUpdateSQL(keySet, valueSet, constraintKey, constraintValue);
            CommonServiceLog.d(TAG, "Built the SQL to query: "+sql);
            
            sCustomerDataManager.executeUpdate(sql, RequestKeys.KEY_CUSTOMER_TYPE, new DataCallback()
            {
                @Override
                public void onSuccess(JSONObject result)
                {
                    try
                    {
                        result.put(ResponseKeys.KEY_RESPONSE_ID, requestID);
                        ManagerHelper.handleResponse(true, ResponseKeys.KEY_CUSTOMER_RESULT, result, response);
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
                    	ManagerHelper.handleResponseWithError(response, ErrorType.valueOf(error
                                .getString(ResponseKeys.KEY_ERROR_MESSAGE)));
                    } catch (JSONException e)
                    {
                    	CommonServiceLog.e(TAG, "JSONException happened when there is an error on getting UPDATE result.", requestID, e);
                    }

                }
            });
        } catch (JSONException e)
        {
            CommonServiceLog.logRequest(TAG, requestID, e.getMessage());
            ManagerHelper.handleResponseWithError(response, ErrorType.BAD_REQUEST);          
            CommonServiceLog.e(TAG, "JSONException happened when getting key/value before UPDATE operation.", e);
        }              
    }    
    
}
