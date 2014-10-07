package com.logpie.service.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import com.logpie.service.config.DatabaseConfig;
import com.logpie.service.util.DatabaseSchema;
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

    }

}
