package com.logpie.service.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.ResponseKeys;
import com.logpie.service.config.ServiceConfig;
import com.logpie.service.error.ErrorMessage;
import com.logpie.service.error.ErrorType;
import com.logpie.service.util.ServiceLog;

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
            ServiceLog.d(TAG, "JDBC is loaded.");

            setConnection(openConnection());
            ServiceLog.d(TAG, "Database is connected.");
        } catch (ClassNotFoundException e)
        {
            ServiceLog
                    .e(TAG,
                            "ClassNotFoundException happended when trying to initialize postgreSQL driver.",
                            e);
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happened when trying to initialize the database");
        }
    }

    private Connection openConnection() throws SQLException
    {
        ServiceLog.d(TAG, "Database is connecting...");
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

    /**
     * Different data manger should implement this function corresponding to
     * each table
     * 
     * @param tableName
     */
    protected abstract void createTable();

    public void executeInsert(String sql, final DataCallback dataCallback)
    {
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "SQL is null or empty when executing INSERT operation.");
            return;
        }

        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            ServiceLog.d(TAG, "Starting to INSERT...");
            statement = sConnection.createStatement();
            int affectedRows = statement.executeUpdate(sql);

            if (affectedRows == 0)
            {
                ServiceLog.e(TAG, "INSERT operation failed. No rows affected.");
                JSONObject error = new JSONObject();
                error.put(ResponseKeys.KEY_RESULT_ERROR_MESSAGE, ErrorMessage.ERROR_INSERT_FAILED);
                handleErrorCallbackWithServerError(error, dataCallback);
            }
            else
            {
                ServiceLog.d(TAG, "INSERT is finished. " + affectedRows + " row affected.");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(ResponseKeys.KEY_REQUEST_TYPE, ResponseKeys.REQUEST_TYPE_INSERT);
                dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happend when executing the INSERT sql", e);
            handleErrorCallbackWithServerError(null, dataCallback);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException happend when executing callback, e");
            handleErrorCallbackWithServerError(null, dataCallback);
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

    public void executeQuery(ArrayList<String> keySet, String sql, final DataCallback dataCallback)
    {
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "SQL is null or empty when executing QUERY operation.");
            return;
        }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            ServiceLog.d(TAG, "Starting to QUERY...");
            statement = sConnection.prepareStatement(sql);
            ServiceLog.d(TAG, "Query SQL: " + sql);
            resultSet = statement.executeQuery();
            if (!resultSet.isBeforeFirst())
            {
                ServiceLog.e(TAG, "QUERY operation failed. No rows found.");
                JSONObject error = new JSONObject();
                error.put(ResponseKeys.KEY_RESULT_ERROR_MESSAGE, ErrorMessage.ERROR_QUERY_FAILED);
                handleErrorCallbackWithServerError(error, dataCallback);
            }
            else
            {
                ServiceLog.d(TAG, "QUERY is finished. ");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(ResponseKeys.KEY_REQUEST_TYPE, ResponseKeys.REQUEST_TYPE_QUERY);
                buildResultSet(keySet, resultSet, returnJSON, dataCallback);
            }
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happend when execute the QUERY sql", e);
            handleErrorCallbackWithServerError(null, dataCallback);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(null, dataCallback);
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

    public String executeSingleQuery(String sql, String keyword)
    {
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "SQL is null or empty when executing single QUERY operation.");
            return null;
        }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        ServiceLog.d(TAG, "Single Query SQL: " + sql);
        try
        {
            ServiceLog.d(TAG, "Starting to QUERY...");
            statement = sConnection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (!resultSet.next())
            {
                ServiceLog.e(TAG, "QUERY operation failed. No rows found.");
            }
            else
            {
                ServiceLog.d(TAG, "QUERY is finished. ");
                Object o = resultSet.getObject(keyword);
                if (o == null)
                {
                    ServiceLog.e(TAG, "Cannot get the return value when using the keyword '"
                            + keyword + "'.");
                }
                else
                {
                    String value = o.toString();
                    resultSet.close();
                    return value;
                }
            }
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happend when execute the QUERY sql", e);
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

    public void executeUpdate(String sql, final DataCallback dataCallback)
    {
        if (sql == null || sql.equals(""))
        {
            ServiceLog.e(TAG, "SQL is null or empty when executing UPDATE operation.");
            return;
        }

        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            sConnection = openConnection();
            ServiceLog.d(TAG, "Starting to UPDATE...");
            statement = sConnection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows == 0)
            {
                ServiceLog.e(TAG, "UPDATE operation failed. No rows affected.");
                JSONObject error = new JSONObject();
                error.put(ResponseKeys.KEY_RESULT_ERROR_MESSAGE, ErrorMessage.ERROR_UPDATE_FAILED);
                handleErrorCallbackWithServerError(error, dataCallback);
            }
            else
            {
                ServiceLog.d(TAG, "UPDATE is finished. " + affectedRows + " row affected.");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(ResponseKeys.KEY_REQUEST_TYPE, ResponseKeys.REQUEST_TYPE_UPDATE);
                dataCallback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happend when execute the UPDATE sql", e);
            handleErrorCallbackWithServerError(null, dataCallback);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException happend when execute callback", e);
            handleErrorCallbackWithServerError(null, dataCallback);
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
            // Means select * from table
            if (keySet == null || keySet.size() == 0)
            {
                buildAllResultSet(resultSet, returnJSON, dataCallback);
                return;
            }

            JSONArray array = new JSONArray();
            while (resultSet.next())
            {
                ServiceLog.d(TAG, "Starting to build result set...");
                JSONObject object = new JSONObject();
                for (String key : keySet)
                {
                    Object o = resultSet.getObject(key);
                    if (o != null)
                    {
                        String returnValue = o.toString();
                        object.put(key, returnValue);
                    }
                    else
                    {
                        ServiceLog.e(TAG, "Cannot get the return value when using the keyword '"
                                + key + "'.");
                    }
                }
                array.put(object);
            }
            returnJSON.put(ResponseKeys.KEY_METADATA, array);
            dataCallback.onSuccess(returnJSON);

        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQLException happend when build the result set", e);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG, "JSONException happend when build the result set", e);
        }
    }

    /**
     * Different data managers should implement this function corresponding to
     * each table
     * 
     * @param resultSet
     * @param returnJSON
     */
    protected abstract void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON,
            DataCallback callback);

    /**
     * Check if the table exists or not
     * If no, create the table
     * If yes, do nothing
     * 
     * */
    protected abstract boolean checkTableExisted();

    private void handleErrorCallbackWithServerError(JSONObject error, DataCallback callback)
    {
        try
        {
            if (error == null)
            {
                error = new JSONObject();
            }
            error.put(ResponseKeys.KEY_SERVER_ERROR_MESSAGE, ErrorType.SEVER_ERROR.toString());
            callback.onError(error);
        } catch (JSONException e)
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

    private void setConnection(Connection connection)
    {
        DataManager.sConnection = connection;
    }
}
