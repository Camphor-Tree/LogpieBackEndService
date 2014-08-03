package com.logpie.customer.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.Driver;

import com.logpie.customer.config.CustomerConfig;
import com.logpie.customer.logic.CustomerResponseKeys;
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
     * initialize the postgreSQL's db driver
     */
    private static void initializeDB()
    {
        try
        {
            sPostgreDriver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
            boolean isExisted = isTableExisted(NAME_USER_TABLE);
            if(!isExisted){
            	createTable(NAME_USER_TABLE);          	
            }
            CommonServiceLog.i(TAG, "Check whether the table '"+ NAME_USER_TABLE +"' is existed: "+String.valueOf(isExisted));
        } catch (InstantiationException e)
        {
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG,
                    "InstantiationException happended when trying to initiliaze postgreSQL driver");
        } catch (IllegalAccessException e)
        {
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG,
                    "IllegalAccessException happended when trying to initiliaze postgreSQL driver");
        } catch (ClassNotFoundException e)
        {
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG,
                    "ClassNotFoundException happended when trying to initiliaze postgreSQL driver");
        }
    }
    
    private static Connection openConnection() throws SQLException
    {
        return DriverManager.getConnection(CustomerConfig.PostgreSQL_URL,
                CustomerConfig.PostgreSQL_Username, CustomerConfig.PostgreSQL_Password);
    }
    
    private static boolean isTableExisted(String tableName){
    	Connection connection;
		try {
			connection = openConnection();
			if(connection!=null){
				DatabaseMetaData metaData = connection.getMetaData();
				if(metaData!=null){
					return metaData.getTables(null, null, tableName, null).last();
				}
			}
			return false;
		}
    	catch (SQLException e) {
			// TODO 
			e.printStackTrace();
			CommonServiceLog.e(TAG,"SQL exception happen when checking table");
			return false;
		}
    }
    
    private static void createTable(String tableName){
    	try {
    		Statement statement = openConnection().createStatement();
    		switch(tableName){
    			case "user":
    				statement.execute(SQLHelper.SQL_CREATE_USER_TABLE);
    			default:
    				return;
    		}
		} catch (SQLException e) {
			e.printStackTrace();
			CommonServiceLog.e(TAG, "SQL error happen when creating a table");
		}
    }
    
    public void executeInsert(String sql, final DataCallback dataCallback){
    	Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next())
            {
                long uid = resultSet.getLong(SQLHelper.SCHEMA_UID);
                String email = resultSet.getString(SQLHelper.SCHEMA_EMAIL);
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(CustomerResponseKeys.KEY_USER_ID, String.valueOf(uid));
                returnJSON.put(CustomerResponseKeys.KEY_EMAIL, email);
                dataCallback.onSuccess(returnJSON);
            }
            else
            {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG, "SQLException happend when executing the sql");
            //handleErrorCallbackWithServerError(callback);
        } catch (JSONException e)
        {
            //handleErrorCallbackWithServerError(callback);
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG, "JSONException happend when executing callback");
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
    
    public void executeFind(String sql, final DataCallback dataCallback){
    	Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Find user failed, no rows affected.");
            }

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next())
            {
                long uid = resultSet.getLong(SQLHelper.SCHEMA_UID);
                String email = resultSet.getString(SQLHelper.SCHEMA_EMAIL);
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(CustomerResponseKeys.KEY_USER_ID, String.valueOf(uid));
                returnJSON.put(CustomerResponseKeys.KEY_EMAIL, email);
                dataCallback.onSuccess(returnJSON);
            }
            else
            {
                throw new SQLException("Find user failed, no generated key obtained.");
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql");
            //handleErrorCallbackWithServerError(callback);
        } catch (JSONException e)
        {
            //handleErrorCallbackWithServerError(callback);
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG, "JSONException happend when executing callback");
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
}
