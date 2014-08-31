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
import com.logpie.service.config.DatabaseConfig;

public class CustomerDataManager extends DataManager
{
    private static final String TAG = CustomerDataManager.class.getName();
    private static CustomerDataManager sCustomerDataManager;
    private static boolean tag = false;

    public static final String USER_TABLE = RequestKeys.KEY_TABLE_USER;

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

    private boolean checkTableExisted()
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
            CommonServiceLog.d(TAG, "Creating '" + USER_TABLE + "' table...");
            statement.execute(DatabaseConfig.SQL_CREATE_USER_TABLE);
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
            long uid = resultSet.getLong(DatabaseSchema.SCHEMA_UID);
            String email = resultSet.getString(DatabaseSchema.SCHEMA_EMAIL);
            String nickname = resultSet.getString(DatabaseSchema.SCHEMA_NICKNAME);
            boolean gender = resultSet.getBoolean(DatabaseSchema.SCHEMA_GENDER);
            Date birthday = resultSet.getDate(DatabaseSchema.SCHEMA_BIRTHDAY);
            String country = resultSet.getString(DatabaseSchema.SCHEMA_COUNTRY);
            int city = resultSet.getInt(DatabaseSchema.SCHEMA_CITY);
            Date lastUpdateTime = resultSet
                    .getDate(DatabaseSchema.SCHEMA_LAST_UPDATE_TIME);
            boolean organization = resultSet
                    .getBoolean(DatabaseSchema.SCHEMA_IS_ORGANIZATION);

            returnJSON.put(ResponseKeys.KEY_UID, String.valueOf(uid));
            returnJSON.put(ResponseKeys.KEY_EMAIL, email);
            returnJSON.put(ResponseKeys.KEY_NICKNAME, nickname);
            returnJSON.put(ResponseKeys.KEY_GENDER, String.valueOf(gender));
            returnJSON.put(ResponseKeys.KEY_BIRTHDAY, String.valueOf(birthday));
            returnJSON.put(ResponseKeys.KEY_COUNTRY, country);
            returnJSON.put(ResponseKeys.KEY_CITY, String.valueOf(city));
            returnJSON.put(ResponseKeys.KEY_LAST_UPDATE_TIME,
                    String.valueOf(lastUpdateTime));
            returnJSON
                    .put(ResponseKeys.KEY_IS_ORGANIZATION, String.valueOf(organization));
        } catch (SQLException e)
        {
            CommonServiceLog
                    .e(TAG,
                            "SQLException happened when building all result set of query request",
                            e);
        } catch (JSONException e)
        {
            CommonServiceLog
                    .e(TAG,
                            "JSONException happened when building all result set of query request",
                            e);
        }
    }
}
