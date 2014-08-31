package com.logpie.service.data;

import java.util.ArrayList;

import com.logpie.service.common.helper.CommonServiceLog;

public class SQLHelper
{
    private static final String TAG = SQLHelper.class.getName();

    public static String buildInsertSQL(String tableName, ArrayList<String> key_set,
            ArrayList<String> value_set)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        CommonServiceLog.d(TAG, "Building sql of INSERT request...");
        if (key_set.size() != value_set.size())
        {
            CommonServiceLog
                    .e(TAG,
                            "The length of keySet is not the same as valueSet when parsing the JSONArray of INSERT request.");
            return null;
        }
        else
        {
            CommonServiceLog.d(TAG,
                    "Parsing the JSONArray of INSERT request to build sql...");
            sqlBuilder.append("insert into ");
            sqlBuilder.append(tableName);
            sqlBuilder.append("(");
            for (int i = 0; i < key_set.size(); i++)
            {
                String key = key_set.get(i);
                sqlBuilder.append(key);
                if (i == key_set.size() - 1)
                {
                    sqlBuilder.append(") values (\' ");
                }
                else
                {
                    sqlBuilder.append(", ");
                }
            }
            for (int i = 0; i < value_set.size(); i++)
            {
                String value = value_set.get(i);
                sqlBuilder.append(value);
                if (i == value_set.size() - 1)
                {
                    sqlBuilder.append("\')");
                }
                else
                {
                    sqlBuilder.append("\', \'");
                }
            }
        }
        return sqlBuilder.toString();
    }

    public static String buildQuerySQL(String tableName, ArrayList<String> key_set,
            String constraint_key, String constraint_value)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        CommonServiceLog.d(TAG, "Building sql of QUERY request...");
        if (key_set == null)
        {
            sqlBuilder.append("select * ");
        }
        else
        {
            CommonServiceLog.d(TAG,
                    "Parsing the JSONArray of QUERY request to build sql...");
            sqlBuilder.append("select ");
            for (int i = 0; i < key_set.size(); i++)
            {
                sqlBuilder.append(key_set.get(i));
                if (i == key_set.size() - 1)
                {
                    sqlBuilder.append(" ");
                }
                else
                {
                    sqlBuilder.append(", ");
                }
            }

        }
        sqlBuilder.append("from user where ");
        sqlBuilder.append(constraint_key);
        sqlBuilder.append(" like \'");
        sqlBuilder.append(constraint_value);
        sqlBuilder.append("\'");
        return sqlBuilder.toString();
    }

    public static String buildUpdateSQL(ArrayList<String> key_set,
            ArrayList<String> value_set, String constraint_key, String constraint_value)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        CommonServiceLog.d(TAG, "Building sql of UPDATE request...");
        if (key_set.size() != value_set.size())
        {
            CommonServiceLog
                    .e(TAG,
                            "The length of keySet is not the same as valueSet when parsing the JSONArray of UPDATE request.");
            return null;
        }
        else
        {
            CommonServiceLog.d(TAG,
                    "Parsing the JSONArray of UPDATE request to build sql...");
            for (int i = 0; i < key_set.size(); i++)
            {
                String key = key_set.get(i);
                String value = value_set.get(i);
                sqlBuilder.append("update user set ");
                sqlBuilder.append(key);
                sqlBuilder.append(" = \'");
                sqlBuilder.append(value);
                if (i == key_set.size() - 1)
                {
                    sqlBuilder.append("\' ");
                }
                else
                {
                    sqlBuilder.append("\', ");
                }
            }
        }
        sqlBuilder.append("where ");
        sqlBuilder.append(constraint_key);
        sqlBuilder.append(" like \'");
        sqlBuilder.append(constraint_value);
        sqlBuilder.append("\'");
        return sqlBuilder.toString();
    }

}
