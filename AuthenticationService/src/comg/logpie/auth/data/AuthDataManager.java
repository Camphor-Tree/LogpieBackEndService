package comg.logpie.auth.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.Driver;

import com.logpie.auth.config.AuthConfig;
import com.logpie.auth.exception.EmailAlreadyExistException;
import com.logpie.auth.logic.AuthResponseKeys;
import com.logpie.auth.tool.AuthErrorType;
import com.logpie.auth.tool.AuthServiceLog;

public class AuthDataManager
{
    public interface DataCallback
    {
        abstract void onSuccess(JSONObject result);

        abstract void onError(JSONObject error);
    }

    private static final String TAG = AuthDataManager.class.getName();
    private static AuthDataManager sAuthDataManager;
    private static Driver sPostgreDriver;

    public static final String KEY_INSERT_RESULT_ID = "com.logpie.auth.insert.id";
    public static final String KEY_CALLBACK_ERROR = "com.logpie.auth.error";

    public synchronized static AuthDataManager getInstance()
    {
        if (sAuthDataManager == null)
        {
            initializeDB();
            sAuthDataManager = new AuthDataManager();
        }
        return sAuthDataManager;
    }

    /**
     * initialize the postgreSQL's db driver
     */
    private static void initializeDB()
    {
        try
        {
            sPostgreDriver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
        } catch (InstantiationException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG,
                    "InstantiationException happended when trying to initiliaze postgreSQL driver");
        } catch (IllegalAccessException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG,
                    "IllegalAccessException happended when trying to initiliaze postgreSQL driver");
        } catch (ClassNotFoundException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG,
                    "ClassNotFoundException happended when trying to initiliaze postgreSQL driver");
        }
    }

    public boolean executeNoResult(String sql)
    {
        Connection connection = null;
        boolean result = false;
        try
        {
            connection = openConnection();
            Statement statement = connection.createStatement();
            result = statement.execute(sql);
            connection.close();
            return result;
        } catch (SQLException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "SQLException happend when execute the sql");
            return false;
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (SQLException e)
                {
                    AuthServiceLog.e(TAG, e.getMessage());
                    AuthServiceLog.e(TAG, "SQLException when trying to close the connection");
                }
            }

        }

    }

    public void executeInsert(final String sql, final DataCallback callback)
            throws EmailAlreadyExistException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try
        {
            connection = openConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
            {
                long id = generatedKeys.getLong(1);
                callback.onSuccess(new JSONObject().put(KEY_INSERT_RESULT_ID, String.valueOf(id)));
            }
            else
            {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException e)
        {

            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "SQLException happend when execute the sql");
            if (e.getMessage().contains("ERROR: duplicate key value"))
            {
                throw new EmailAlreadyExistException();
            }
            else
            {
                handleErrorCallbackWithServerError(callback);
            }
        } catch (JSONException e)
        {
            handleErrorCallbackWithServerError(callback);
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "JSONException happend when executing callback");
        } finally
        {
            if (generatedKeys != null)
                try
                {
                    generatedKeys.close();
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
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }

    // Step1 Query the user information, to verify the email_password
    public JSONObject executeQueryForLoginStep1(final String sql)
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            resultSet = statement.executeQuery(sql);
            int count = 0;
            if (resultSet.last())
            {
                count = resultSet.getRow();
                resultSet.beforeFirst(); // not rs.first() because the rs.next()
                                         // below
                // will move on, missing the first element
            }
            // no user found, return auth error.
            if (count == 0)
            {
                return null;
            }
            else
            {
                resultSet.next();
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(AuthResponseKeys.KEY_USER_ID,
                        String.valueOf(resultSet.getLong(SQLHelper.SCHEMA_UID)));
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN,
                        resultSet.getString(SQLHelper.SCHEMA_ACCESS_TOKEN));
                return returnJSON;
            }
        } catch (SQLException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "SQLException happend when execute the sql");
            return null;

        } catch (JSONException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "JSONException happend when executing callback");
            return null;

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
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }

    // Step2, update the token
    public boolean executeQueryForLoginStep2(final String sql)
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = openConnection();
            statement = connection.createStatement();
            int row_affected = statement.executeUpdate(sql);
            if (row_affected != 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        } catch (SQLException e)
        {
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "SQLException happend when execute the sql");
            return false;
        } finally
        {
            if (statement != null)
                try
                {
                    statement.close();
                } catch (SQLException logOrIgnore)
                {
                }
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }

    // Step3 Query the user information again, get lastest token information
    public void executeQueryForLoginStep3(final String sql, final DataCallback callback)
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            resultSet = statement.executeQuery(sql);
            int count = 0;
            if (resultSet.last())
            {
                count = resultSet.getRow();
                resultSet.beforeFirst(); // not rs.first() because the rs.next()
                                         // below
                // will move on, missing the first element
            }
            // no user found, return auth error.
            if (count == 0)
            {
                callback.onError(new JSONObject().append(KEY_CALLBACK_ERROR,
                        AuthErrorType.AUTH_ERROR));
            }
            else
            {
                resultSet.next();
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(AuthResponseKeys.KEY_USER_ID,
                        String.valueOf(resultSet.getLong(SQLHelper.SCHEMA_UID)));
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN,
                        resultSet.getString(SQLHelper.SCHEMA_ACCESS_TOKEN));
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION, resultSet
                        .getTimestamp(SQLHelper.SCHEMA_ACCESS_TOKEN_EXPIRATION).toString());
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN,
                        resultSet.getString(SQLHelper.SCHEMA_REFRESH_TOKEN));
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION, resultSet
                        .getTimestamp(SQLHelper.SCHEMA_REFRESH_TOKEN_EXPIRATION).toString());
                callback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            handleErrorCallbackWithServerError(callback);
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "SQLException happend when execute the sql");
        } catch (JSONException e)
        {
            handleErrorCallbackWithServerError(callback);
            AuthServiceLog.e(TAG, e.getMessage());
            AuthServiceLog.e(TAG, "JSONException happend when executing callback");
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
            if (connection != null)
                try
                {
                    connection.close();
                } catch (SQLException logOrIgnore)
                {
                }
        }
    }

    private Connection openConnection() throws SQLException
    {
        return DriverManager.getConnection(AuthConfig.PostgreSQL_URL,
                AuthConfig.PostgreSQL_Username, AuthConfig.PostgreSQL_Password);
    }

    private void handleErrorCallbackWithServerError(DataCallback callback)
    {
        try
        {
            callback.onError(new JSONObject().append(KEY_CALLBACK_ERROR,
                    AuthErrorType.SEVER_ERROR.toString()));
        } catch (JSONException e1)
        {
        }
    }
}
