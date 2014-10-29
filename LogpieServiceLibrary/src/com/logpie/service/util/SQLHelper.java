package com.logpie.service.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SQLHelper
{
    private static final String TAG = SQLHelper.class.getName();

    public static String buildInsertSQL(String tableName, Map<String, String> keyvaluePair)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of INSERT request...");
        if (tableName == null || tableName.equals(""))
        {
            ServiceLog.e(TAG, "The table name cannot be null or empty.");
            return null;
        }
        if (keyvaluePair == null || keyvaluePair.isEmpty())
        {
            ServiceLog.e(TAG, "The keyvalue pair cannot be null.");
            return null;
        }

        ServiceLog.d(TAG, "Parsing the JSONArray of INSERT request to build sql...");
        Set<String> keys = keyvaluePair.keySet();
        Iterator<String> i = keys.iterator();

        sqlBuilder.append("insert into \"");
        sqlBuilder.append(tableName);
        sqlBuilder.append("\" (");

        StringBuilder values = new StringBuilder();
        values.append("values (");

        while (i.hasNext())
        {
            String key = i.next();
            // append key
            sqlBuilder.append(key);
            // append value
            if (key.equals(DatabaseSchema.SCHEMA_ACTIVITY_LATLON))
            {
                values.append(keyvaluePair.get(key));
            }
            else
            {
                values.append("'");
                values.append(keyvaluePair.get(key));
                values.append("'");
            }

            // check if key/value is the last one
            if (i.hasNext())
            {
                sqlBuilder.append(", ");
                values.append(", ");
            }
            else
            {
                sqlBuilder.append(") ");
                values.append(")");
            }
        }

        sqlBuilder.append(values);
        sqlBuilder.append(";");

        return sqlBuilder.toString();
    }

    public static String buildQuerySQL(ArrayList<String> tableName, ArrayList<String> key_set,
            Map<String, Map<String, String>> constraints, Map<String, String> tableLinkConstraint,
            String number, String orderBy, boolean isASC)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of QUERY request...");

        if (tableName == null || tableName.size() == 0)
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
        if (tableName.size() == 1)
        {
            sqlBuilder.append("\"");
            sqlBuilder.append(tableName.get(0));
            sqlBuilder.append("\"");
        }
        else
        {
            int size = tableName.size();
            for (int i = 0; i < size; i++)
            {
                sqlBuilder.append("\"");
                sqlBuilder.append(tableName.get(i));
                if (i == size - 1)
                {
                    sqlBuilder.append("\"");
                }
                else
                {
                    sqlBuilder.append("\",");
                }
            }
        }

        String constraintSQL = buildConstraintSQL(constraints, tableLinkConstraint);
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
            sqlBuilder.append(" ORDER BY ");
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

    public static String buildUpdateSQL(String tableName, Map<String, String> keyvaluePair,
            Map<String, Map<String, String>> constraints)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        ServiceLog.d(TAG, "Building sql of UPDATE request...");
        if (tableName == null || tableName.equals(""))
        {
            ServiceLog.e(TAG, "The table name cannot be null or empty.");
            return null;
        }
        if (keyvaluePair == null || keyvaluePair.isEmpty())
        {
            ServiceLog.e(TAG, "The key value pair cannot be null.");
            return null;
        }

        ServiceLog.d(TAG, "Parsing the JSONArray of UPDATE request to build sql...");
        sqlBuilder.append("update \"");
        sqlBuilder.append(tableName);
        sqlBuilder.append("\" set ");

        Set<String> keys = keyvaluePair.keySet();
        Iterator<String> i = keys.iterator();

        while (i.hasNext())
        {
            String key = i.next();
            // append key
            sqlBuilder.append(key);
            sqlBuilder.append("=");
            // append value
            if (key.equals(DatabaseSchema.SCHEMA_ACTIVITY_LATLON))
            {
                sqlBuilder.append(keyvaluePair.get(key));
            }
            else
            {
                sqlBuilder.append("'");
                sqlBuilder.append(keyvaluePair.get(key));
                sqlBuilder.append("'");
            }

            // check if key/value is the last one
            if (i.hasNext())
            {
                sqlBuilder.append(", ");
            }
        }

        String constraintSQL = buildConstraintSQL(constraints, null);
        if (constraintSQL == null || constraintSQL.equals(""))
        {
            ServiceLog.d(TAG, "Constraint SQL is null.");
        }
        else
        {
            sqlBuilder.append(constraintSQL);
        }

        sqlBuilder.append(";");
        return sqlBuilder.toString();
    }

    private static String buildConstraintSQL(Map<String, Map<String, String>> constraints,
            Map<String, String> linkTableConstraint)
    {
        if (constraints == null || constraints.isEmpty())
        {
            ServiceLog.e(TAG, "There is no key value mappings in Constraint Keyvalue Pair.");
            return null;
        }

        Set<String> keys = constraints.keySet();
        Iterator<String> iterator = keys.iterator();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" where ");

        // Add talbe link clause to the sql. For examle: select * from
        // user,comment where "user".uid=activity.user_id
        if (linkTableConstraint != null && linkTableConstraint.size() > 0)
        {
            Set<String> keySet = linkTableConstraint.keySet();
            for (String key : keySet)
            {
                sqlBuilder.append(key);
                sqlBuilder.append("=");
                sqlBuilder.append(linkTableConstraint.get(key));
            }

            if (constraints != null && constraints.size() > 0)
            {
                sqlBuilder.append(" AND ");
            }
            else
            {
                sqlBuilder.append(" ");
            }
        }

        while (iterator.hasNext())
        {
            String key = iterator.next();
            Map<String, String> map = constraints.get(key);
            if (map.isEmpty())
            {
                ServiceLog.e(TAG, "There is no operator mappings in Constraint Keyvalue Pair.");
                return null;
            }

            Set<String> operators = map.keySet();
            if (operators.size() == 0 || operators.size() > 1)
            {
                ServiceLog.e(TAG,
                        "There is error in operator mappings of Constraint Keyvalue Pair.");
                return null;
            }
            String operator = operators.iterator().next();
            String value = map.get(operator);
            sqlBuilder.append(key);
            sqlBuilder.append(operator);
            if (key.equals(DatabaseSchema.SCHEMA_ACTIVITY_LATLON))
            {
                sqlBuilder.append(value);
            }
            else
            {
                sqlBuilder.append("'");
                sqlBuilder.append(value);
                sqlBuilder.append("'");
            }

            if (iterator.hasNext())
            {
                sqlBuilder.append(" AND ");
            }
        }

        return sqlBuilder.toString();
    }
}
