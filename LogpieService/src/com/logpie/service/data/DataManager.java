package com.logpie.service.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.error.ErrorType;
import com.logpie.service.common.helper.CommonServiceLog;
import com.logpie.service.common.helper.ResponseKeys;
import com.logpie.service.config.ServiceConfig;

public abstract class DataManager
{

    private static final String TAG = DataManager.class.getName();
    private static Connection sConnection;

    public DataManager()
    {
        initializeDB();
    }

    /**
     * Initialize the postgreSQL's db driver and connect the database
     */
    private void initializeDB()
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            CommonServiceLog.d(TAG, "JDBC is loaded.");

            setsConnection(openConnection());
            CommonServiceLog.d(TAG, "Database is connected.");
        } catch (ClassNotFoundException e)
        {
            CommonServiceLog
                    .e(TAG,
                            "ClassNotFoundException happended when trying to initiliaze postgreSQL driver.",
                            e);
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Connection openConnection() throws SQLException
    {
        CommonServiceLog.d(TAG, "Database is connecting...");
        return DriverManager.getConnection(ServiceConfig.PostgreSQL_URL,
                ServiceConfig.PostgreSQL_Username, ServiceConfig.PostgreSQL_Password);
    }

    protected boolean checkTableExisted(String tableName)
    {
        try
        {
            if (sConnection != null)
            {
                DatabaseMetaData metaData = sConnection.getMetaData();
                if (metaData != null)
                {
                    CommonServiceLog.d(TAG, "Checking table '" + tableName + "'...");
                    return metaData.getTables(null, null, tableName, null).last();
                }
                else
                {
                    CommonServiceLog.e(TAG, "Cannot get metadata from database.");
                }
            }
            else
            {
                CommonServiceLog.e(TAG, "Cannot connect to the database.");
            }
            return false;
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQL exception happen when checking table", e);
            return false;
        }
    }

    /**
     * Different data manger should implement this function corresponding to
     * each table
     * 
     * @param tableName
     */
    protected abstract void createTable();

    public void executeInsert(String sql, String resultType,
            final DataCallback dataCallback)
    {
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            CommonServiceLog.d(TAG, "Starting to INSERT...");
            statement = sConnection.createStatement();
            int affectedRows = statement.executeUpdate(sql);

            if (affectedRows == 0)
            {
                CommonServiceLog.e(TAG, "INSERT operation failed. No rows affected.");
                JSONObject error = new JSONObject();
                error.put(resultType, ResponseKeys.KEY_RESULT_ERROR);
                dataCallback.onError(error);
            }
            else
            {
                CommonServiceLog.d(TAG, "INSERT is finished. " + affectedRows
                        + " row affected.");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(resultType, ResponseKeys.KEY_RESULT_SUCCESS);
                dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when executing the INSERT sql",
                    e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException happend when executing callback, e");
            handleErrorCallbackWithServerError(dataCallback);
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
        }
    }

    public void executeQuery(ArrayList<String> keySet, String sql, String resultType,
            final DataCallback dataCallback)
    {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            CommonServiceLog.d(TAG, "Starting to QUERY...");
            statement = sConnection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                CommonServiceLog.e(TAG, "QUERY operation failed. No rows affected.");
                JSONObject error = new JSONObject();
                error.put(resultType, ResponseKeys.KEY_RESULT_ERROR);
                dataCallback.onError(error);
            }
            else
            {
                CommonServiceLog.d(TAG, "QUERY is finished. " + affectedRows
                        + " row(s) affected.");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(resultType, ResponseKeys.KEY_RESULT_SUCCESS);
                resultSet = statement.getGeneratedKeys();
                buildResultSet(keySet, resultSet, returnJSON, dataCallback);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the QUERY sql", e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(dataCallback);
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
        }
    }

    public String executeQuery(String sql, String keyword)
    {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            CommonServiceLog.d(TAG, "Starting to QUERY...");
            statement = sConnection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                CommonServiceLog.e(TAG, "QUERY operation failed. No rows affected.");
                return null;
            }
            else
            {
                CommonServiceLog.d(TAG, "QUERY is finished. " + affectedRows
                        + " row(s) affected.");
                resultSet = statement.getGeneratedKeys();
                return resultSet.getString(keyword);
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the QUERY sql", e);
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
        }
        return null;
    }

    public void executeUpdate(String sql, String resultType,
            final DataCallback dataCallback)
    {
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            sConnection = openConnection();
            CommonServiceLog.d(TAG, "Starting to UPDATE...");
            statement = sConnection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows == 0)
            {
                CommonServiceLog.e(TAG, "UPDATE operation failed. No rows affected.");
                JSONObject error = new JSONObject();
                error.put(resultType, ResponseKeys.KEY_RESULT_ERROR);
                dataCallback.onError(error);
            }
            else
            {
                CommonServiceLog.d(TAG, "UPDATE is finished. " + affectedRows
                        + " row affected.");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(resultType, ResponseKeys.KEY_RESULT_SUCCESS);
                dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            CommonServiceLog
                    .e(TAG, "SQLException happend when execute the UPDATE sql", e);
            handleErrorCallbackWithServerError(dataCallback);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(dataCallback);
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
        }
    }

    protected void buildResultSet(ArrayList<String> keySet, ResultSet resultSet,
            JSONObject returnJSON, final DataCallback dataCallback)
    {
        try
        {
            if (resultSet.next())
            {
                CommonServiceLog.d(TAG, "Starting to build result set...");
                if (keySet == null) // Means find * from table...
                {
                    buildAllResultSet(resultSet, returnJSON);
                }
                else
                {
                    for (String key : keySet)
                    {
                        String returnValue = resultSet.getString(key);
                        returnJSON.put(key, returnValue);
                    }
                }
                dataCallback.onSuccess(returnJSON);
            }
            else
            {
                throw new SQLException(
                        "Building result set failed. No generated key obtained.");
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when build the result set", e);
        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException happend when build the result set", e);
        }
    }

    /**
     * Different data managers should implement this function corresponding to
     * each table
     * 
     * @param resultSet
     * @param returnJSON
     */
    protected abstract void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON);

    private void handleErrorCallbackWithServerError(DataCallback callback)
    {
        try
        {
            callback.onError(new JSONObject().append(ResponseKeys.KEY_ERROR_MESSAGE,
                    ErrorType.SEVER_ERROR.toString()));
        } catch (JSONException e1)
        {
        }
    }

    /**
     * Getter and Setter
     * 
     * @return Connection
     */
    public Connection getsConnection()
    {
        return sConnection;
    }

    private void setsConnection(Connection sConnection)
    {
        DataManager.sConnection = sConnection;
    }
}
