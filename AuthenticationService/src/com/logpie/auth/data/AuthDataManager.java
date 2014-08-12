package com.logpie.auth.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.auth.config.AuthConfig;
import com.logpie.auth.exception.EmailAlreadyExistException;
import com.logpie.auth.logic.AuthResponseKeys;
import com.logpie.auth.logic.TokenGenerator;
import com.logpie.auth.tool.AuthErrorType;
import com.logpie.service.common.helper.CommonServiceLog;

public class AuthDataManager
{
    public interface DataCallback
    {
        abstract void onSuccess(JSONObject result);

        abstract void onError(JSONObject error);
    }

    private static final String TAG = AuthDataManager.class.getName();
    private static AuthDataManager sAuthDataManager;

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
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (InstantiationException e)
        {
            CommonServiceLog.e(TAG,
                    "InstantiationException happended when trying to initiliaze postgreSQL driver", e);
        } catch (IllegalAccessException e)
        {
            CommonServiceLog.e(TAG,
                    "IllegalAccessException happended when trying to initiliaze postgreSQL driver", e);
        } catch (ClassNotFoundException e)
        {
            CommonServiceLog.e(TAG,
                    "ClassNotFoundException happended when trying to initiliaze postgreSQL driver", e);
        }
    }

    public boolean executeNoResult(String sql)
    {
    	initializeDB();
        Connection connection = null;
        try
        {
            connection = openConnection();
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(sql);
            connection.close();
            return result==1?true:false;
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
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
                    CommonServiceLog.e(TAG, "SQLException when trying to close the connection", e);
                }
            }

        }

    }

    public void executeInsertAndGetUIDandEmail(final String sql, final DataCallback callback) throws EmailAlreadyExistException
    {
    	Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next())
            {
                long uid = resultSet.getLong(SQLHelper.SCHEMA_UID);
                String email = resultSet.getString(SQLHelper.SCHEMA_EMAIL);
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(AuthResponseKeys.KEY_USER_ID, String.valueOf(uid));
                returnJSON.put(AuthResponseKeys.KEY_EMAIL, email);
                callback.onSuccess(returnJSON);
            }
            else
            {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
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
            CommonServiceLog.e(TAG, e.getMessage());
            CommonServiceLog.e(TAG, "JSONException happend when executing callback");
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
    
    public void executeInsertAndGetWholeRecord(final String sql, final DataCallback callback)
            throws EmailAlreadyExistException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = openConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next())
            {
                long uid = resultSet.getLong(SQLHelper.SCHEMA_UID);
                String email = resultSet.getString(SQLHelper.SCHEMA_EMAIL);
                String access_token = resultSet.getString(SQLHelper.SCHEMA_ACCESS_TOKEN);
                String access_token_expiration = resultSet
                        .getString(SQLHelper.SCHEMA_ACCESS_TOKEN_EXPIRATION);
                String refresh_token = resultSet.getString(SQLHelper.SCHEMA_REFRESH_TOKEN);
                String refresh_token_expiration = resultSet
                        .getString(SQLHelper.SCHEMA_REFRESH_TOKEN_EXPIRATION);
                JSONObject returnJSON = new JSONObject();
                returnJSON.put(AuthResponseKeys.KEY_USER_ID, String.valueOf(uid));
                returnJSON.put(AuthResponseKeys.KEY_EMAIL, email);
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN, access_token);
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION,
                        access_token_expiration);
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN, refresh_token);
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION,
                        refresh_token_expiration);
                callback.onSuccess(returnJSON);
            }
            else
            {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
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
            CommonServiceLog.e(TAG, "JSONException happend when executing callback", e);
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

    public boolean executeQueryForCheckDuplicate(final String sql)
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
            
            if(resultSet.last())
            {
            	CommonServiceLog.e(TAG, "The user is already existed!");
            	return true;
            }else
            {
            	return false;
            }
        }catch(SQLException e)
        {
        	CommonServiceLog.e(TAG, "SQLException happend when execute the sql to check user duplicate", e);
        	return true;
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
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN,
                        resultSet.getString(SQLHelper.SCHEMA_REFRESH_TOKEN));
                returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION,
                        resultSet.getTimestamp(SQLHelper.SCHEMA_ACCESS_TOKEN_EXPIRATION).toString());
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION,
                        resultSet.getTimestamp(SQLHelper.SCHEMA_REFRESH_TOKEN_EXPIRATION).toString());
                return returnJSON;
            }
        } catch (SQLException e)
        {
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
            return null;

        } catch (JSONException e)
        {
            CommonServiceLog.e(TAG, "JSONException happend when executing callback", e);
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
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
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

    // Step4 Query the user information again, get lastest token information
    /**
     * Step4 have been optimized to based on Step1 ~ Step3's results
     * @param sql
     * @param callback
     */
    @Deprecated
    public void executeQueryForLoginStep4(final String sql, final DataCallback callback)
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
                //We doesn't plan to include expiration information to clients.
                //Since User may login to different device. We will refresh the token anyway.
                //Then the expiration time the client's hold may not be accurate. So just let the server to handle token check.
                //returnJSON.put(AuthResponseKeys.KEY_ACCESS_TOKEN_EXPIRATION, resultSet.getTimestamp(SQLHelper.SCHEMA_ACCESS_TOKEN_EXPIRATION).toString());
                returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN,
                        resultSet.getString(SQLHelper.SCHEMA_REFRESH_TOKEN));
                //returnJSON.put(AuthResponseKeys.KEY_REFRESH_TOKEN_EXPIRATION, resultSet.getTimestamp(SQLHelper.SCHEMA_REFRESH_TOKEN_EXPIRATION).toString());
                callback.onSuccess(returnJSON);
            }
        } catch (SQLException e)
        {
            handleErrorCallbackWithServerError(callback);
            CommonServiceLog.e(TAG, "SQLException happend when execute the sql", e);
        } catch (JSONException e)
        {
            handleErrorCallbackWithServerError(callback);
            CommonServiceLog.e(TAG, "JSONException happend when executing callback", e);
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
