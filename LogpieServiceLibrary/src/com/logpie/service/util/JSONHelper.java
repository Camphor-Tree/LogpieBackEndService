package com.logpie.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.RequestKeys;

public class JSONHelper
{
    private static final String TAG = JSONHelper.class.getName();

    public static JSONArray buildInsertKeyValue(Map<String, String> insertKeyvalue)
            throws JSONException
    {
        if (insertKeyvalue.isEmpty())
        {
            ServiceLog.e(TAG, "There is no key value mappings in Insert Keyvalue Pair.");
            return null;
        }

        JSONArray array = new JSONArray();
        Set<String> keys = insertKeyvalue.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext())
        {
            String key = i.next();
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_INSERT_COLUMN, key);
            o.put(RequestKeys.KEY_INSERT_VALUE, insertKeyvalue.get(key));
            array.put(o);
        }
        return array;
    }

    public static JSONArray buildUpdateKeyValue(Map<String, String> updateKeyvalue)
            throws JSONException
    {
        if (updateKeyvalue.isEmpty())
        {
            ServiceLog.e(TAG, "There is no key value mappings in Update Keyvalue Pair.");
            return null;
        }

        JSONArray array = new JSONArray();
        Set<String> keys = updateKeyvalue.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext())
        {
            String key = i.next();
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_UPDATE_COLUMN, key);
            o.put(RequestKeys.KEY_UPDATE_VALUE, updateKeyvalue.get(key));
            array.put(o);
        }
        return array;
    }

    public static JSONArray buildQueryKey(List<String> columns) throws JSONException
    {
        JSONArray array = new JSONArray();
        if (columns == null)
        {
            return array;
        }

        for (int i = 0; i < columns.size(); i++)
        {
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_QUERY_COLUMN, columns.get(i));
            array.put(o);
        }
        return array;
    }

    public static JSONArray buildConstraintKeyValue(Map<String, Map<String, String>> constraints)
            throws JSONException
    {
        if (constraints.isEmpty())
        {
            ServiceLog.e(TAG, "There is no key value mappings in Constraint Keyvalue Pair.");
            return null;
        }

        JSONArray array = new JSONArray();
        Set<String> keys = constraints.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext())
        {
            JSONObject o = new JSONObject();
            String key = i.next();
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

            o.put(RequestKeys.KEY_CONSTRAINT_COLUMN, key);
            o.put(RequestKeys.KEY_CONSTRAINT_OPERATOR, operator);
            o.put(RequestKeys.KEY_CONSTRAINT_VALUE, value);
            array.put(o);
        }
        return array;
    }

    public static String parseToSQL(JSONObject postData, ArrayList<String> keySet, String table,
            String requestType, ArrayList<String> returnSet) throws JSONException
    {
        switch (requestType)
        {
        case "insert":
            return parseInsertRequest(postData, keySet, table);
        case "query":
            return parseQueryRequest(postData, returnSet, table);
        case "update":
            return parseUpdateRequest(postData, table);
        default:
            ServiceLog.e(TAG, "Unsupported request type.");
            break;
        }
        return null;
    }

    private static String parseInsertRequest(JSONObject postData, ArrayList<String> requiredKeySet,
            String table) throws JSONException
    {
        Map<String, String> keyvalue = new HashMap<String, String>();

        JSONArray insertKeyvaluePair = postData.getJSONArray(RequestKeys.KEY_INSERT_KEYVALUE_PAIR);

        for (int i = 0; i < insertKeyvaluePair.length(); i++)
        {
            if (insertKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_INSERT_COLUMN)
                    && insertKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_INSERT_VALUE))
            {
                keyvalue.put(
                        insertKeyvaluePair.getJSONObject(i)
                                .getString(RequestKeys.KEY_INSERT_COLUMN), insertKeyvaluePair
                                .getJSONObject(i).getString(RequestKeys.KEY_INSERT_VALUE));
            }
        }

        // Check if any required key is existed in the JSON data
        // If not, will throw JSONException
        Set<String> keys = keyvalue.keySet();
        checkKeyParameterExisted(requiredKeySet, keys);
        ServiceLog.d(TAG, "Parsed the INSERT request.");

        return SQLHelper.buildInsertSQL(table, keyvalue);

    }

    private static String parseQueryRequest(JSONObject postData, ArrayList<String> returnSet,
            String table) throws JSONException
    {
        JSONArray queryKey = postData.getJSONArray(RequestKeys.KEY_QUERY_KEY);

        for (int i = 0; i < queryKey.length(); i++)
        {
            if (queryKey.getJSONObject(i).has(RequestKeys.KEY_QUERY_COLUMN))
            {
                returnSet.add(queryKey.getJSONObject(i).getString(RequestKeys.KEY_QUERY_COLUMN));
            }
        }

        Map<String, Map<String, String>> constraints = new HashMap<String, Map<String, String>>();

        parseConstraint(postData, constraints);

        String number = null;
        if (postData.has(RequestKeys.KEY_LIMIT_NUMBER))
        {
            number = postData.getString(RequestKeys.KEY_LIMIT_NUMBER);
        }

        String orderBy = null;
        boolean isASC = true;
        if (postData.has(RequestKeys.KEY_ORDER_BY))
        {
            orderBy = postData.getString(RequestKeys.KEY_ORDER_BY);
            if (postData.has(RequestKeys.KEY_DESC))
            {
                isASC = false;
            }
        }

        return SQLHelper.buildQuerySQL(table, returnSet, constraints, number, orderBy, isASC);
    }

    private static String parseUpdateRequest(JSONObject postData, String table)
            throws JSONException
    {
        Map<String, String> keyvalue = new HashMap<String, String>();

        JSONArray updateKeyvaluePair = postData.getJSONArray(RequestKeys.KEY_UPDATE_KEYVALUE_PAIR);

        for (int i = 0; i < updateKeyvaluePair.length(); i++)
        {
            if (updateKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_UPDATE_COLUMN)
                    && updateKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_UPDATE_VALUE))
            {
                keyvalue.put(
                        updateKeyvaluePair.getJSONObject(i)
                                .getString(RequestKeys.KEY_UPDATE_COLUMN), updateKeyvaluePair
                                .getJSONObject(i).getString(RequestKeys.KEY_UPDATE_VALUE));
            }
        }

        Map<String, Map<String, String>> constraints = new HashMap<String, Map<String, String>>();

        parseConstraint(postData, constraints);

        return SQLHelper.buildUpdateSQL(table, keyvalue, constraints);

    }

    private static void parseConstraint(JSONObject postData,
            Map<String, Map<String, String>> constraints) throws JSONException
    {
        if (!postData.has(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR))
        {
            ServiceLog.d(TAG, "There is no constraint key value pair in the request.");
            return;
        }

        JSONArray constraintKeyvaluePair = postData
                .getJSONArray(RequestKeys.KEY_CONSTRAINT_KEYVALUE_PAIR);

        for (int i = 0; i < constraintKeyvaluePair.length(); i++)
        {
            JSONObject data = constraintKeyvaluePair.getJSONObject(i);
            if (data.has(RequestKeys.KEY_CONSTRAINT_COLUMN)
                    && data.has(RequestKeys.KEY_CONSTRAINT_OPERATOR)
                    && data.has(RequestKeys.KEY_CONSTRAINT_VALUE))
            {
                Map<String, String> hm = new HashMap<String, String>();
                hm.put(data.getString(RequestKeys.KEY_CONSTRAINT_OPERATOR),
                        data.getString(RequestKeys.KEY_CONSTRAINT_VALUE));
                constraints.put(data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN), hm);
            }
        }
    }

    public static void checkKeyParameterExisted(List<String> requiredParameters,
            Set<String> parameters) throws JSONException
    {
        if (parameters == null || requiredParameters == null)
        {
            ServiceLog.e(TAG, "JSON data or require parameter is null.");
            return;
        }

        HashSet<String> requiredhs = new HashSet<String>();
        requiredhs.addAll(requiredParameters);

        if (!parameters.containsAll(requiredhs))
        {
            throw new JSONException("JSONException happened when check key parameters.");
        }

        return;
    }

    public static void checkValueIsNull(List<String> values) throws JSONException
    {
        if (values == null)
        {
            ServiceLog.e(TAG, "Value of JSON data is null.");
            return;
        }

        for (String value : values)
        {
            if (value == null || value.equals(""))
                throw new JSONException("JSONException happened when check value parameters.");
        }

    }
}
