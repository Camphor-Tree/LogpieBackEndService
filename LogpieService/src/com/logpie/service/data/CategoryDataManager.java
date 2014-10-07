package com.logpie.service.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import com.logpie.service.config.DatabaseConfig;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.ServiceLog;

public class CategoryDataManager extends DataManager
{
    private static final String TAG = CityDataManager.class.getName();
    private static final String CATEGORY_TABLE = DatabaseSchema.SCHEMA_TABLE_CATEGORY;
    private static final String SUBCATEGORY_TABLE = DatabaseSchema.SCHEMA_TABLE_SUBCATEGORY;

    private static CategoryDataManager sCategoryDataManager;
    private static boolean tag = false;

    /**
     * CustomerDataManager is singleton
     */
    private CategoryDataManager()
    {
        super();

        if (!checkTableExisted())
        {
            createTable();
        }
    }

    public synchronized static CategoryDataManager getInstance()
    {
        if (sCategoryDataManager == null)
        {
            sCategoryDataManager = new CategoryDataManager();
        }
        return sCategoryDataManager;
    }

    private boolean checkTableExisted()
    {
        if (!tag)
        {
            if (checkTableExisted(CATEGORY_TABLE) && checkTableExisted(SUBCATEGORY_TABLE))
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
            ServiceLog.d(TAG, "Creating '" + CATEGORY_TABLE + "' and '" + SUBCATEGORY_TABLE
                    + " table...");
            statement.execute(DatabaseConfig.SQL_CREATE_CATEGORY_TABLE);
            statement.execute(DatabaseConfig.SQL_INSERT_CATEGORY_TABLE);
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
