package com.logpie.service.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.config.DatabaseConfig;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.ResponseKeys;
import com.logpie.service.util.ServiceLog;

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
            ServiceLog.d(TAG, "Creating '" + ACTIVITY_TABLE + "' table...");
            statement.execute(DatabaseConfig.SQL_CREATE_ACTIVITY_TABLE);
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQL error happen when creating a table", e);
        }
    }

    @Override
    protected void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON,
            DataCallback dataCallback)
    {
        try
        {
            JSONArray array = new JSONArray();

            while (resultSet.next())
            {
                ServiceLog.d(TAG, "Starting to build result set...");
                JSONObject object = new JSONObject();

                long aid = resultSet.getLong(DatabaseSchema.SCHEMA_ACTIVITY_AID);
                long uid = resultSet.getLong(DatabaseSchema.SCHEMA_ACTIVITY_CREATE_UID);
                String description = resultSet
                        .getString(DatabaseSchema.SCHEMA_ACTIVITY_DESCRIPTION);
                String location = resultSet
                        .getString(DatabaseSchema.SCHEMA_ACTIVITY_LOCATION);
                String createTime = resultSet.getTimestamp(
                        DatabaseSchema.SCHEMA_ACTIVITY_CREATE_TIME).toString();
                String startTime = resultSet.getTimestamp(
                        DatabaseSchema.SCHEMA_ACTIVITY_START_TIME).toString();
                String endTime = resultSet.getTimestamp(
                        DatabaseSchema.SCHEMA_ACTIVITY_END_TIME).toString();
                int city = resultSet.getInt(DatabaseSchema.SCHEMA_ACTIVITY_CITY);
                double lat = resultSet.getDouble(DatabaseSchema.SCHEMA_ACTIVITY_LATITUDE);
                double lon = resultSet
                        .getDouble(DatabaseSchema.SCHEMA_ACTIVITY_LONGITUDE);
                int category = resultSet.getInt(DatabaseSchema.SCHEMA_ACTIVITY_CATEGORY);
                int countLike = resultSet
                        .getInt(DatabaseSchema.SCHEMA_ACTIVITY_COUNT_LIKE);
                int countDislike = resultSet
                        .getInt(DatabaseSchema.SCHEMA_ACTIVITY_COUNT_DISLIKE);

                object.put(ResponseKeys.KEY_AID, String.valueOf(aid));
                object.put(ResponseKeys.KEY_UID, String.valueOf(uid));
                object.put(ResponseKeys.KEY_DESCRIPTION, description);
                object.put(ResponseKeys.KEY_LOCATION, location);
                object.put(ResponseKeys.KEY_CREATE_TIME, createTime);
                object.put(ResponseKeys.KEY_START_TIME, startTime);
                object.put(ResponseKeys.KEY_END_TIME, endTime);
                object.put(ResponseKeys.KEY_CITY, String.valueOf(city));
                object.put(ResponseKeys.KEY_LATITUDE, String.valueOf(lat));
                object.put(ResponseKeys.KEY_LONGITUDE, String.valueOf(lon));
                object.put(ResponseKeys.KEY_CATEGORY, String.valueOf(category));
                object.put(ResponseKeys.KEY_COUNT_LIKE, String.valueOf(countLike));
                object.put(ResponseKeys.KEY_COUNT_DISLIKE, String.valueOf(countDislike));

                array.put(object);
            }

            returnJSON.put(ResponseKeys.KEY_METADATA, array);
            dataCallback.onSuccess(returnJSON);

        } catch (SQLException e)
        {
            ServiceLog
                    .e(TAG,
                            "SQLException happened when building all result set of query request",
                            e);
        } catch (JSONException e)
        {
            ServiceLog
                    .e(TAG,
                            "JSONException happened when building all result set of query request",
                            e);
        }
    }
}
