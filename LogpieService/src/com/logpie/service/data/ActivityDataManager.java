package com.logpie.service.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.ResponseKeys;
import com.logpie.service.config.DatabaseConfig;

public class ActivityDataManager extends DataManager
{
    private static final String TAG = ActivityDataManager.class.getName();
    private static ActivityDataManager sActivityDataManager;
    private static boolean tag = false;

    public static final String ACTIVITY_TABLE = DatabaseSchema.SCHEMA_TABLE_ACTIVITY;

    /**
     * CustomerDataManager is singleton
     */
    private ActivityDataManager()
    {
        super();

        if (!checkTableExisted())
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
        if (!tag)
        {
            if (checkTableExisted(ACTIVITY_TABLE))
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
            statement.execute(DatabaseConfig.SQL_CREATE_ACTIVITY_TABLE);
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQL error happen when creating a table", e);
        }
    }

    @Override
    protected void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON)
    {
        try
        {
            long aid = resultSet.getLong(DatabaseSchema.SCHEMA_ACTIVITY_ID);
            long uid = resultSet.getLong(DatabaseSchema.SCHEMA_UID);
            String description = resultSet.getString(DatabaseSchema.SCHEMA_DESCRIPTION);
            String location = resultSet.getString(DatabaseSchema.SCHEMA_LOCATION);
            String createTime = resultSet.getTimestamp(DatabaseSchema.SCHEMA_CREATE_TIME)
                    .toString();
            String startTime = resultSet.getTimestamp(DatabaseSchema.SCHEMA_START_TIME).toString();
            String endTime = resultSet.getTimestamp(DatabaseSchema.SCHEMA_END_TIME).toString();
            int city = resultSet.getInt(DatabaseSchema.SCHEMA_CITY);
            double lat = resultSet.getDouble(DatabaseSchema.SCHEMA_LATITUDE);
            double lon = resultSet.getDouble(DatabaseSchema.SCHEMA_LONGITUDE);
            int category = resultSet.getInt(DatabaseSchema.SCHEMA_CATEGORY);
            int countLike = resultSet.getInt(DatabaseSchema.SCHEMA_COUNT_LIKE);
            int countDislike = resultSet.getInt(DatabaseSchema.SCHEMA_COUNT_DISLIKE);

            returnJSON.put(ResponseKeys.KEY_AID, String.valueOf(aid));
            returnJSON.put(ResponseKeys.KEY_UID, String.valueOf(uid));
            returnJSON.put(ResponseKeys.KEY_DESCRIPTION, description);
            returnJSON.put(ResponseKeys.KEY_LOCATION, location);
            returnJSON.put(ResponseKeys.KEY_CREATE_TIME, createTime);
            returnJSON.put(ResponseKeys.KEY_START_TIME, startTime);
            returnJSON.put(ResponseKeys.KEY_END_TIME, endTime);
            returnJSON.put(ResponseKeys.KEY_CITY, String.valueOf(city));
            returnJSON.put(ResponseKeys.KEY_LATITUDE, String.valueOf(lat));
            returnJSON.put(ResponseKeys.KEY_LONGITUDE, String.valueOf(lon));
            returnJSON.put(ResponseKeys.KEY_CATEGORY, String.valueOf(category));
            returnJSON.put(ResponseKeys.KEY_COUNT_LIKE, String.valueOf(countLike));
            returnJSON.put(ResponseKeys.KEY_COUNT_DISLIKE, String.valueOf(countDislike));

        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG,
                    "SQLException happened when building all result set of query request", e);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG,
                    "JSONException happened when building all result set of query request", e);
        }
    }
}
