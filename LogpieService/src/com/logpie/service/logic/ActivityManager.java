package com.logpie.service.logic;

import javax.servlet.ServletContext;

import com.logpie.service.data.ActivityDataManager;
import com.logpie.service.data.DataManager;

public class ActivityManager {
	private static String TAG = ActivityManager.class.getName();
	private static ActivityManager sActivityManager;
    private static ServletContext sGlobalUniqueContext;
    private static DataManager sDataManager;
    
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
        sDataManager = ActivityDataManager.getInstance();
    }
    
    /**
     * Parse the JSON request to an Activity instance
     * 
     
    private Activity parsingDataToActivity(JSONObject postData)
    {
    	String id = null;
    	String description = null;
    	Location location = null;
    	String startTime = null;
        String endTime = null; 
        List<Comment> comments = null;
        
        if(postData.has(RequestKeys.KEY_DESCRIPTION))
        {
        	try {
				description = postData.getString(RequestKeys.KEY_DESCRIPTION);
			} catch (JSONException e) {
				CommonServiceLog.e(TAG, "Failed to parse activity request when getting the description.", e);
			}
        }
        if(postData.has(RequestKeys.KEY_LOCATION))
        {
        	try {
				JSONObject data = postData.getJSONObject(RequestKeys.KEY_LOCATION);
				String city = null;
				Double lat = null;
				Double lon = null;
				String address = null;
				if(data.has(RequestKeys.KEY_CITY))
				{
					city = data.getString(RequestKeys.KEY_CITY);
				}
				if(data.has(RequestKeys.KEY_LATITUDE) && data.has(RequestKeys.KEY_LONGITUDE))
				{
					lat = Double.valueOf(data.getString(RequestKeys.KEY_LATITUDE));
					lon = Double.valueOf(data.getString(RequestKeys.KEY_LONGITUDE));
				}
				if(data.has(RequestKeys.KEY_ADDRESS))
				{
					address = data.getString(RequestKeys.KEY_ADDRESS);
				}
				location = new Location(lat, lon, city, address);
			} catch (JSONException e) {
				CommonServiceLog.e(TAG, "Failed to parse activity request when getting the location.", e);
			}
        }
        if(postData.has(RequestKeys.KEY_START_TIME))
        {
        	try {
				startTime = postData.getString(RequestKeys.KEY_START_TIME);
			} catch (JSONException e) {
				CommonServiceLog.e(TAG, "Failed to parse activity request when getting the start time.", e);
			}
        }
        if(postData.has(RequestKeys.KEY_END_TIME))
        {
        	try {
				endTime = postData.getString(RequestKeys.KEY_END_TIME);
			} catch (JSONException e) {
				CommonServiceLog.e(TAG, "Failed to parse activity request when getting the end time.", e);
			}
        }
        
    	return new Activity(id, description, location, startTime, endTime, comments);
    }
    
    
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    {
        JSONObject postBody = null;
		try {
			postBody = HttpRequestParser.httpRequestParser(request);
		} catch (HttpRequestIsNullException e) {
			CommonServiceLog.e(TAG, "HttpRequestParser is null when parsing an activity service request.", e);
			return;
		}
        if (postBody != null)
        {
        	String requestType = RequestKeys.KEY_ACTIVITY_TYPE;
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
    
    
   
    private void handleInsert(JSONObject postData, final HttpServletResponse response, final String requestID)
    {
    	try
        {
    		String creator = postData.getString(RequestKeys.KEY_CREATE_USER);
        	String description = postData.getString(RequestKeys.KEY_DESCRIPTION);
            String location = postData.getString(RequestKeys.KEY_LOCATION);
            
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
    
    */
}
