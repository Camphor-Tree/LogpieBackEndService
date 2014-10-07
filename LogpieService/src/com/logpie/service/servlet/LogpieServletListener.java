package com.logpie.service.servlet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.logpie.service.config.DatabaseConfig;
import com.logpie.service.config.ServiceConfig;
import com.logpie.service.util.DatabaseSchema;
import com.logpie.service.util.ServiceLog;

public class LogpieServletListener implements ServletContextListener
{
    private static final String TAG = LogpieServletListener.class.getName();

    private Connection con;

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        initializeDB();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0)
    {

    }

    private void initializeDB()
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            con = openConnection();
            if (!checkTableExisted(con, DatabaseSchema.SCHEMA_TABLE_CITY))
            {
                Statement statement = con.createStatement();
                ServiceLog.d(TAG, "Creating '" + DatabaseSchema.SCHEMA_TABLE_CITY + "' table...");
                statement.execute(DatabaseConfig.SQL_CREATE_CITY_TABLE);
                statement.execute(DatabaseConfig.SQL_INSERT_CITY_TABLE);
            }
            if (!checkTableExisted(con, DatabaseSchema.SCHEMA_TABLE_CATEGORY)
                    || !checkTableExisted(con, DatabaseSchema.SCHEMA_TABLE_SUBCATEGORY))
            {
                Statement statement = con.createStatement();
                ServiceLog.d(TAG, "Creating '" + DatabaseSchema.SCHEMA_TABLE_CATEGORY + "' and '"
                        + DatabaseSchema.SCHEMA_TABLE_SUBCATEGORY + " table...");
                statement.execute(DatabaseConfig.SQL_CREATE_CATEGORY_TABLE);
                statement.execute(DatabaseConfig.SQL_INSERT_CATEGORY_TABLE);
            }

        } catch (ClassNotFoundException e)
        {
            ServiceLog
                    .e(TAG,
                            "ClassNotFoundException happended when trying to initialize postgreSQL driver.",
                            e);
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happened when trying to initialize the database");
        } finally
        {
            try
            {
                if (!con.isClosed())
                {
                    con.close();
                }
            } catch (SQLException e)
            {
                ServiceLog.d(TAG, "Database cannot be closed.");
            }
        }
    }

    private Connection openConnection() throws SQLException
    {
        ServiceLog.d(TAG, "Database is connecting...");
        return DriverManager.getConnection(ServiceConfig.PostgreSQL_URL,
                ServiceConfig.PostgreSQL_Username, ServiceConfig.PostgreSQL_Password);
    }

    protected boolean checkTableExisted(Connection con, String tableName)
    {
        try
        {
            if (con != null)
            {
                DatabaseMetaData metaData = con.getMetaData();
                if (metaData != null)
                {
                    ServiceLog.d(TAG, "Checking table '" + tableName + "'...");
                    return metaData.getTables(null, null, tableName, null).last();
                }
                else
                {
                    ServiceLog.e(TAG, "Cannot get metadata from database.");
                }
            }
            else
            {
                ServiceLog.e(TAG, "Cannot connect to the database.");
            }
            return false;
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQL exception happen when checking table", e);
            return false;
        }
    }

}
