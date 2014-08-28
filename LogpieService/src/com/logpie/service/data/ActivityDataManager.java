package com.logpie.service.data;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.RequestKeys;
import com.logpie.service.common.helper.ResponseKeys;

public class ActivityDataManager extends DataManager
{
    private static final String TAG = ActivityDataManager.class.getName();
    private static ActivityDataManager sActivityDataManager;
    private static boolean tag = false;
    
    public static final String ACTIVITY_TABLE = RequestKeys.KEY_TABLE_ACTIVITY;

    /**
     *  CustomerDataManager is singleton
     */
    private ActivityDataManager()
    {
    	super();
    	
    	if(!checkTableExisted())
    	{
    		createTable();
    	}
    }
    
    public synchronized static ActivityDataManager getInstance()
    {
        if (sActivityDataManager == null)
        {
            sActivityDataManager = new ActivityDataManager();
        }
        return sActivityDataManager;
    }

    
    private boolean checkTableExisted()
    {
    	if(!tag)
    	{
    		if(checkTableExisted(ACTIVITY_TABLE))
    		{
    			tag = true;    			
    		}    		
    	}
    	return tag;   		
    }
    
    @Override
    protected void createTable()
    {
    	try 
    	{
    		Statement statement = getsConnection().createStatement();
    		CommonServiceLog.d(TAG, "Creating '" + ACTIVITY_TABLE + "' table...");
    		//statement.execute(SQLHelper.SQL_CREATE_USER_TABLE);
		} catch (SQLException e) 
		{
			CommonServiceLog.e(TAG, "SQL error happen when creating a table", e);
		}
    }
    
    @Override
    protected void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON)
    {   	
		
    }
}
