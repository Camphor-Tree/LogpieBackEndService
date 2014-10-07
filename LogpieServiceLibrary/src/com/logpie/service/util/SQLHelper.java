package com.logpie.service.util;

import java.util.ArrayList;

public class SQLHelper
{
    private static final String TAG = SQLHelper.class.getName();

    public static String buildInsertSQL(String tableName, ArrayList<String> key_set,
            ArrayList<String> value_set)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of INSERT request...");
        if (tableName == null || tableName.equals(""))
        {
            ServiceLog.e(TAG, "The table name cannot be null or empty.");
            return null;
        }
        if (key_set == null || value_set == null)
        {
            ServiceLog.e(TAG, "The keySet or valueSet cannot be null.");
            return null;
        }
        if (key_set.size() != value_set.size())
        {
            ServiceLog
                    .e(TAG,
                            "The length of keySet is not the same as valueSet when parsing the JSONArray of INSERT request.");
            return null;
        }

        ServiceLog.d(TAG, "Parsing the JSONArray of INSERT request to build sql...");
        sqlBuilder.append("insert into \"");
        sqlBuilder.append(tableName);
        sqlBuilder.append("\" (");
        for (int i = 0; i < key_set.size(); i++)
        {
            String key = key_set.get(i);
            sqlBuilder.append(key);
            if (i == key_set.size() - 1)
            {
                sqlBuilder.append(") values (\'");
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
        sqlBuilder.append(";");

        return sqlBuilder.toString();
    }

    public static String buildQuerySQL(String tableName, ArrayList<String> key_set,
            ArrayList<String> constraint_keys, ArrayList<String> constraint_operators,
            ArrayList<String> constraint_values, String number, String orderBy, boolean isASC)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of QUERY request...");

        if (tableName == null || tableName.equals(""))
        {
            ServiceLog.e(TAG, "The table name cannot be null or empty.");
            return null;
        }

        ServiceLog.d(TAG, "Parsing the JSONArray of QUERY request to build sql...");
        if (key_set == null || key_set.size() == 0)
        {
            ServiceLog.d(TAG, "Key set is null or empty that means it should query all keys.");
            sqlBuilder.append("select * ");
        }
        else
        {
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

        sqlBuilder.append("from ");
        sqlBuilder.append(tableName);

        String constraintSQL = buildConstraintSQL(constraint_keys, constraint_operators,
                constraint_values);
        if (constraintSQL == null || constraintSQL.equals(""))
        {
            ServiceLog.d(TAG, "Constraint SQL is null.");
        }
        else
        {
            sqlBuilder.append(constraintSQL);
        }

        if (orderBy != null)
        {
            sqlBuilder.append("ORDER BY ");
            sqlBuilder.append(orderBy);
            sqlBuilder.append(isASC ? " ASC" : " DESC");
        }

        if (number != null)
        {
            sqlBuilder.append(" limit ");
            sqlBuilder.append(number);
        }

        sqlBuilder.append(";");
        return sqlBuilder.toString();
    }

    public static String buildUpdateSQL(String tableName, ArrayList<String> key_set,
            ArrayList<String> value_set, ArrayList<String> constraint_keys,
            ArrayList<String> constraint_operators, ArrayList<String> constraint_values)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of UPDATE request...");
        if (tableName == null || tableName.equals(""))
        {
            ServiceLog.e(TAG, "The table name cannot be null or empty.");
            return null;
        }
        if (key_set == null || value_set == null)
        {
            ServiceLog.e(TAG, "The keySet or valueSet cannot be null.");
            return null;
        }
        if (key_set.size() != value_set.size())
        {
            ServiceLog
                    .e(TAG,
                            "The length of keySet is not the same as valueSet when parsing the JSONArray of UPDATE request.");
            return null;
        }

        ServiceLog.d(TAG, "Parsing the JSONArray of UPDATE request to build sql...");
        sqlBuilder.append("update ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" set ");
        for (int i = 0; i < key_set.size(); i++)
        {
            sqlBuilder.append(key_set.get(i));
            sqlBuilder.append("=\'");
            sqlBuilder.append(value_set.get(i));
            if (i == key_set.size() - 1)
            {
                sqlBuilder.append("\' ");
            }
            else
            {
                sqlBuilder.append("\', ");
            }
        }

        String constraintSQL = buildConstraintSQL(constraint_keys, constraint_operators,
                constraint_values);
        if (constraintSQL == null || constraintSQL.equals(""))
        {
            ServiceLog.d(TAG, "Constraint SQL is null.");
        }
        else
        {
            sqlBuilder.append(constraintSQL);
        }
        return sqlBuilder.toString();
    }

    private static String buildConstraintSQL(ArrayList<String> constraint_keys,
            ArrayList<String> constraint_operators, ArrayList<String> constraint_values)
    {
        if (constraint_keys != null && constraint_values != null && constraint_operators != null
                && constraint_keys.size() != 0 && constraint_values.size() != 0
                && constraint_operators.size() != 0)
        {
            if (constraint_keys.size() == constraint_values.size()
                    && constraint_keys.size() == constraint_operators.size())
            {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append(" where ");

                for (int i = 0; i < constraint_keys.size(); i++)
                {
                    sqlBuilder.append(constraint_keys.get(i));
                    if (i == constraint_keys.size() - 1)
                    {
                        sqlBuilder.append(constraint_operators.get(i));
                        sqlBuilder.append("\'");
                        sqlBuilder.append(constraint_values.get(i));
                        sqlBuilder.append("\'");
                    }
                    else
                    {
                        sqlBuilder.append(constraint_operators.get(i));
                        sqlBuilder.append("\'");
                        sqlBuilder.append(constraint_values.get(i));
                        sqlBuilder.append("\' AND ");
                    }
                }
                return sqlBuilder.toString();
            }
            ServiceLog.e(TAG, "The length of each parameter list is not the same as others.");
            return null;
        }
        ServiceLog
                .d(TAG, "Cannot get constraint key and constrint value when building a query sql");
        return null;
    }
}
