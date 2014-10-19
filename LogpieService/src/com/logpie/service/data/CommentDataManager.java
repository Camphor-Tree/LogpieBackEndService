package com.logpie.service.data;

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

public class CommentDataManager extends DataManager
{

    private static final String TAG = CommentDataManager.class.getName();
    public static final String TABLE_NAME = DatabaseSchema.SCHEMA_TABLE_COMMENTS;
    private static boolean table_exists = false;
    private static CommentDataManager sCommentDataManager;

    private CommentDataManager()
    {
        super();
        if (!checkTableExisted())
        {
            createTable();
        }
    }

    public synchronized static CommentDataManager getInstance()
    {
        if (sCommentDataManager == null)
        {
            sCommentDataManager = new CommentDataManager();
        }
        return sCommentDataManager;
    }

    @Override
    protected void createTable()
    {
        try
        {
            Statement statement = getsConnection().createStatement();
            ServiceLog.d(TAG, "Creating '" + TABLE_NAME + "' table...");
            statement.execute(DatabaseConfig.SQL_CREATE_COMMENT_TABLE);
            ServiceLog.d(TAG, "Created " + TABLE_NAME + "succeed!");
        } catch (SQLException e)
        {
            ServiceLog.e(TAG, "SQL error happen when creating" + TABLE_NAME + "table", e);
        }

    }

    @Override
    protected void buildAllResultSet(ResultSet resultSet, JSONObject returnJSON,
            DataCallback callback)
    {
        try
        {
            JSONArray array = new JSONArray();

            while (resultSet.next())
            {
                ServiceLog.d(TAG, "Starting to build result set...");
                JSONObject object = new JSONObject();

                long user_id = resultSet.getLong(DatabaseSchema.SCHEMA_COMMENTS_USER_ID);
                long activity_id = resultSet.getLong(DatabaseSchema.SCHEMA_COMMENTS_ACTIVITY_ID);
                String content = resultSet
                        .getString(DatabaseSchema.SCHEMA_COMMENTS_COMMENT_CONTENT);
                String comment_time = resultSet.getTimestamp(
                        DatabaseSchema.SCHEMA_COMMENTS_COMMENT_TIME).toString();

                object.put(ResponseKeys.KEY_SENDER_USER_ID, String.valueOf(user_id));
                object.put(ResponseKeys.KEY_SEND_TO_ACTIVITYID, String.valueOf(activity_id));
                object.put(ResponseKeys.KEY_COMMENT_CONTENT, String.valueOf(content));
                object.put(ResponseKeys.KEY_COMMENT_TIME, String.valueOf(comment_time));

                array.put(object);
            }

            returnJSON.put(ResponseKeys.KEY_METADATA, array);
            callback.onSuccess(returnJSON);

        } catch (SQLException e)
        {
            ServiceLog.e(TAG,
                    "SQLException happened when building all result set of query request", e);
        } catch (JSONException e)
        {
            ServiceLog.e(TAG,
                    "JSONException happened when building all result set of query request", e);
        }
    }

    @Override
    protected boolean checkTableExisted()
    {
        if (!table_exists)
        {
            if (checkTableExisted(TABLE_NAME))
            {
                table_exists = true;
            }
        }
        return table_exists;
    }

}
