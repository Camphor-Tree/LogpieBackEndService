package com.logpie.customer.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.Driver;

import com.logpie.customer.config.CustomerConfig;
import com.logpie.customer.logic.CustomerResponseKeys;
import com.logpie.customer.tool.CustomerErrorType;
import com.logpie.service.common.helper.CommonServiceLog;

public class CustomerDataManager
{
    public interface DataCallback
    {
        abstract void onSuccess(JSONObject result);
        abstract void onError(JSONObject error);
    }

    private static final String TAG = CustomerDataManager.class.getName();
    private static CustomerDataManager sAuthDataManager;
    private static Driver sPostgreDriver;

    public static final String KEY_INSERT_RESULT_ID = "com.logpie.auth.insert.id";
    public static final String KEY_CALLBACK_ERROR = "com.logpie.auth.error";
    public static final String NAME_USER_TABLE = "user";

    /**
     *  CustomerDataManager is singleton
     */
    private CustomerDataManager()
    {
    	
    }
    
    public synchronized static CustomerDataManager getInstance()
    {
        if (sAuthDataManager == null)
        {
            initializeDB();
            sAuthDataManager = new CustomerDataManager();
        }
        return sAuthDataManager;
    }

    /**
     * Initialize the postgreSQL's db driver
     * Check User table
     */
    private static void initializeDB()
    {
        try
        {
            sPostgreDriver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
            
            boolean tag = isTableExisted(NAME_USER_TABLE);
            CommonServiceLog.d(TAG, "Is table '"+ NAME_USER_TABLE +"' existed? "+String.valueOf(tag));            
            if(!tag)
            {
            	createTable(NAME_USER_TABLE);
            }           
        } catch (InstantiationException e)
        {
            CommonServiceLog.e(TAG,
                    "InstantiationException happended when trying to initiliaze postgreSQL driver.", e);
        } catch (IllegalAccessException e)
        {
            CommonServiceLog.e(TAG,
                    "IllegalAccessException happended when trying to initiliaze postgreSQL driver.", e);
        } catch (ClassNotFoundException e)
        {
            CommonServiceLog.e(TAG,
                    "ClassNotFoundException happended when trying to initiliaze postgreSQL driver.", e);
        }
    }
    
    private static Connection openConnection() throws SQLException
    {
    	CommonServiceLog.d(TAG,"Database is connecting...");
        return DriverManager.getConnection(CustomerConfig.PostgreSQL_URL,
                CustomerConfig.PostgreSQL_Username, CustomerConfig.PostgreSQL_Password);
    }
    
    private static boolean isTableExisted(String tableName)
    {
    	Connection connection;
		try {
			connection = openConnection();
			if(connection!=null)
			{
				DatabaseMetaData metaData = connection.getMetaData();
				if(metaData!=null)
				{
					CommonServiceLog.d(TAG,"Checking table '" + tableName + "'...");
					return metaData.getTables(null, null, tableName, null).last();
				}else
				{
					CommonServiceLog.e(TAG, "Cannot get metadata from database.");
				}
			}else
			{
				CommonServiceLog.e(TAG, "Cannot connect to the database.");
			}
			return false;
		}catch (SQLException e) 
    	{
			CommonServiceLog.e(TAG,"SQL exception happen when checking table", e);
			return false;
		}
    }
    
    private static void createTable(String tableName)
    {
    	try {
    		Statement statement = openConnection().createStatement();
    		switch(tableName)
    		{
    			case "user":
    				CommonServiceLog.d(TAG, "Creating '" + tableName + "' table...");
    				statement.execute(SQLHelper.SQL_CREATE_USER_TABLE);   				
    			default:
    				CommonServiceLog.e(TAG, "Unsupported table name in the database.");
    				return;
    		}
		} catch (SQLException e) 
		{
			CommonServiceLog.e(TAG, "SQL error happen when creating a table", e);
		}
    }
    
    public void executeInsert(String sql, final DataCallback dataCallback)
    {
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            CommonServiceLog.d(TAG, "Starting to INSERT...");
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            
            if (affectedRows == 0)
            {
            	CommonServiceLog.e(TAG, "INSERT operation failed. No rows affected.");
            	JSONObject error = new JSONObject();
            	error.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_ERROR);
                dataCallback.onError(error);
            }else
            {
            	CommonServiceLog.d(TAG,"INSERT is finished. " + affectedRows + " row affected.");
            	JSONObject returnJSON = new JSONObject();
            	returnJSON.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_SUCCESS);
            	dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when executing the INSERT sql", e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e)
        {          
            CommonServiceLog.e(TAG, "JSONException happend when executing callback, e");
            handleErrorCallbackWithServerError(dataCallback);
        } finally
        {
            if (resultSet != null)
                try
                {
                    resultSet.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (statement != null)
                try
                {
                    statement.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }
    
    public void executeQuery(ArrayList<String> keySet, String sql, final DataCallback dataCallback){
    	Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            connection = openConnection();
            CommonServiceLog.d(TAG, "Starting to QUERY...");
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
            	CommonServiceLog.e(TAG, "QUERY operation failed. No rows affected.");
            	JSONObject error = new JSONObject();
            	error.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_ERROR);				 
                dataCallback.onError(error);
            }else
            {            	
            	CommonServiceLog.d(TAG,"QUERY is finished. " + affectedRows + " row(s) affected.");
            	JSONObject returnJSON = new JSONObject();
            	returnJSON.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_SUCCESS);
            	resultSet = statement.getGeneratedKeys();           	
            	buildResultSet(keySet, resultSet, returnJSON, dataCallback);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the QUERY sql", e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e) {
        	CommonServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(dataCallback);
		} finally
        {
            if (resultSet != null)
                try
                {
                    resultSet.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (statement != null)
                try
                {
                    statement.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }
    
    public void executeUpdate(String sql, final DataCallback dataCallback){
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            connection = openConnection();
            CommonServiceLog.d(TAG, "Starting to UPDATE...");
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);            
            if (affectedRows == 0)
            {
            	CommonServiceLog.e(TAG, "UPDATE operation failed. No rows affected.");
            	JSONObject error = new JSONObject();
            	error.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_ERROR);
                dataCallback.onError(error);
            }else
            {
            	CommonServiceLog.d(TAG,"UPDATE is finished. " + affectedRows + " row affected.");
            	JSONObject returnJSON = new JSONObject();
            	returnJSON.put(CustomerResponseKeys.KEY_CUSTOMER_RESULT, CustomerResponseKeys.CUSTOMER_RESULT_SUCCESS);
            	dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the UPDATE sql", e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e) {
        	CommonServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(dataCallback);
		} finally
        {
            if (resultSet != null)
                try
                {
                    resultSet.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (statement != null)
                try
                {
                    statement.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }
    
    private void buildResultSet(ArrayList<String> keySet, ResultSet resultSet, JSONObject returnJSON, final DataCallback dataCallback)
    {
    	try {
			if (resultSet.next())
			{
				CommonServiceLog.d(TAG, "Starting to build result set...");
				
				if(keySet==null)
				{
					long uid = resultSet.getLong(SQLHelper.SCHEMA_UID);
					String email = resultSet.getString(SQLHelper.SCHEMA_EMAIL);
					String nickname = resultSet.getString(SQLHelper.SCHEMA_NICKNAME);
					boolean gender = resultSet.getBoolean(SQLHelper.SCHEMA_GENDER);
					Date birthday = resultSet.getDate(SQLHelper.SCHEMA_BIRTHDAY);
					String country = resultSet.getString(SQLHelper.SCHEMA_country);
					int city = resultSet.getInt(SQLHelper.SCHEMA_CITY);
					Date lastUpdateTime = resultSet.getDate(SQLHelper.SCHEMA_LASTUPDATEDTIME);
					boolean organization = resultSet.getBoolean(SQLHelper.SCHEMA_ISORGANIZATION);
			    
					returnJSON.put(CustomerResponseKeys.KEY_USER_ID, String.valueOf(uid));
					returnJSON.put(CustomerResponseKeys.KEY_EMAIL, email);
					returnJSON.put(CustomerResponseKeys.KEY_NICKNAME, nickname);
					returnJSON.put(CustomerResponseKeys.KEY_GENDER, String.valueOf(gender));
					returnJSON.put(CustomerResponseKeys.KEY_BIRTHDAY, String.valueOf(birthday));
					returnJSON.put(CustomerResponseKeys.KEY_COUNTRY, country);
					returnJSON.put(CustomerResponseKeys.KEY_CITY, String.valueOf(city));
					returnJSON.put(CustomerResponseKeys.KEY_LASTUPDATETIME, String.valueOf(lastUpdateTime));
					returnJSON.put(CustomerResponseKeys.KEY_ISORGANIZATION, String.valueOf(organization));
				}else
				{
					for(String key : keySet)
					{
						String returnValue = resultSet.getString(key);
						returnJSON.put(key, returnValue);
					}
				}
			    dataCallback.onSuccess(returnJSON);
			}else
			{
			    throw new SQLException("Building result set failed. No generated key obtained.");
			}
		} catch (SQLException e) {
			CommonServiceLog.e(TAG, "SQLException happend when build the result set", e);
		} catch (JSONException e) {
			CommonServiceLog.e(TAG, "JSONException happend when build the result set", e);
		}
    }
    
    private void handleErrorCallbackWithServerError(DataCallback callback)
    {
        try
        {
            callback.onError(new JSONObject().append(KEY_CALLBACK_ERROR,
                    CustomerErrorType.SEVER_ERROR.toString()));
        } catch (JSONException e1)
        {
        }
    }
}
