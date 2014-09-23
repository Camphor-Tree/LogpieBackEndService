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

public class CityDataManager extends DataManager
{
    private static final String TAG = CityDataManager.class.getName();
    private static final String CITY_TABLE = DatabaseSchema.SCHEMA_TABLE_CITY;

    private static CityDataManager sCityDataManager;
    private static boolean tag = false;

    /**
     * CustomerDataManager is singleton
     */
    private CityDataManager()
    {
        super();

        if (!checkTableExisted())
        {
            createTable();
        }
    }

    public synchronized static CityDataManager getInstance()
    {
        if (sCityDataManager == null)
        {
            sCityDataManager = new CityDataManager();
        }
        return sCityDataManager;
    }

    private boolean checkTableExisted()
    {
        if (!tag)
        {
            if (checkTableExisted(CITY_TABLE))
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
            ServiceLog.d(TAG, "Creating '" + CITY_TABLE + "' table...");
            statement.execute(DatabaseConfig.SQL_CREATE_CITY_TABLE);
            statement.execute(DatabaseConfig.SQL_INSERT_CITY_TABLE);
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

                long cid = resultSet.getLong(DatabaseSchema.SCHEMA_CITY_CID);
                String city = resultSet.getString(DatabaseSchema.SCHEMA_CITY_CITY);
                int grade = resultSet.getInt(DatabaseSchema.SCHEMA_CITY_GRADE);
                String province = resultSet
                        .getString(DatabaseSchema.SCHEMA_CITY_PROVINCE);

                object.put(ResponseKeys.KEY_AID, String.valueOf(cid));
                object.put(ResponseKeys.KEY_DESCRIPTION, city);
                object.put(ResponseKeys.KEY_UID, String.valueOf(grade));
                object.put(ResponseKeys.KEY_LOCATION, province);

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
