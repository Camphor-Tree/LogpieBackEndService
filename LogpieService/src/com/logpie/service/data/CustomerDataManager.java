package com.logpie.service.data;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.config.DatabaseConfig;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.ServiceLog;

public class CustomerDataManager extends DataManager
{
    private static final String TAG = CustomerDataManager.class.getName();
    private static CustomerDataManager sCustomerDataManager;
    private static boolean tag = false;

    public static final String USER_TABLE = DatabaseSchema.SCHEMA_TABLE_USER;

    /**
     * CustomerDataManager is singleton
     */
    private CustomerDataManager()
    {
        super();

        if (!checkTableExisted())
        {
            createTable();
        }
    }

    public synchronized static CustomerDataManager getInstance()
    {
        if (sCustomerDataManager == null)
        {
            sCustomerDataManager = new CustomerDataManager();
        }
        return sCustomerDataManager;
    }

    @Override
    protected boolean checkTableExisted()
    {
        if (!tag)
        {
            if (checkTableExisted(USER_TABLE))
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
            ServiceLog.d(TAG, "Creating '" + USER_TABLE + "' table...");
            statement.execute(DatabaseConfig.SQL_CREATE_USER_TABLE);
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

                long uid = resultSet.getLong(DatabaseSchema.SCHEMA_USER_UID);
                String email = resultSet.getString(DatabaseSchema.SCHEMA_USER_EMAIL);
                String nickname = resultSet
                        .getString(DatabaseSchema.SCHEMA_USER_NICKNAME);
                boolean gender = resultSet.getBoolean(DatabaseSchema.SCHEMA_USER_GENDER);
                Date birthday = resultSet.getDate(DatabaseSchema.SCHEMA_USER_BIRTHDAY);
                String country = resultSet.getString(DatabaseSchema.SCHEMA_USER_COUNTRY);
                int city = resultSet.getInt(DatabaseSchema.SCHEMA_USER_CITY);
                Date lastUpdateTime = resultSet
                        .getDate(DatabaseSchema.SCHEMA_USER_LAST_UPDATE_TIME);
                boolean organization = resultSet
                        .getBoolean(DatabaseSchema.SCHEMA_USER_IS_ORGANIZATION);

                object.put(ResponseKeys.KEY_UID, String.valueOf(uid));
                object.put(ResponseKeys.KEY_EMAIL, email);
                object.put(ResponseKeys.KEY_NICKNAME, nickname);
                object.put(ResponseKeys.KEY_GENDER, String.valueOf(gender));
                object.put(ResponseKeys.KEY_BIRTHDAY, String.valueOf(birthday));
                object.put(ResponseKeys.KEY_COUNTRY, country);
                object.put(ResponseKeys.KEY_CITY, String.valueOf(city));
                object.put(ResponseKeys.KEY_LAST_UPDATE_TIME,
                        String.valueOf(lastUpdateTime));
                object.put(ResponseKeys.KEY_IS_ORGANIZATION, String.valueOf(organization));

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
