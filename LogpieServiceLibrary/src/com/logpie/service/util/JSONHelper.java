package com.logpie.service.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.commonlib.RequestKeys;

public class JSONHelper
{
    private static final String TAG = JSONHelper.class.getName();

    public static JSONArray buildInsertKeyValue(ArrayList<String> columns, ArrayList<String> values)
            throws JSONException
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < columns.size(); i++)
        {
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_INSERT_COLUMN, columns.get(i));
            o.put(RequestKeys.KEY_INSERT_VALUE, values.get(i));
            array.put(o);
        }
        return array;
    }

    public static JSONArray buildUpdateKeyValue(ArrayList<String> columns, ArrayList<String> values)
            throws JSONException
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < columns.size(); i++)
        {
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_UPDATE_COLUMN, columns.get(i));
            o.put(RequestKeys.KEY_UPDATE_VALUE, values.get(i));
            array.put(o);
        }
        return array;
    }

    public static JSONArray buildQueryKey(ArrayList<String> columns) throws JSONException
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

    public static JSONArray buildConstraintKeyValue(ArrayList<String> columns,
            ArrayList<String> operators, ArrayList<String> values) throws JSONException
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < columns.size(); i++)
        {
            JSONObject o = new JSONObject();
            o.put(RequestKeys.KEY_CONSTRAINT_COLUMN, columns.get(i));
            o.put(RequestKeys.KEY_CONSTRAINT_OPERATOR, operators.get(i));
            o.put(RequestKeys.KEY_CONSTRAINT_VALUE, values.get(i));
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
        ArrayList<String> keySet = new ArrayList<String>();
        ArrayList<String> valueSet = new ArrayList<String>();

        JSONArray insertKeyvaluePair = postData.getJSONArray(RequestKeys.KEY_INSERT_KEYVALUE_PAIR);

        for (int i = 0; i < insertKeyvaluePair.length(); i++)
        {
            if (insertKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_INSERT_COLUMN)
                    && insertKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_INSERT_VALUE))
            {
                keySet.add(insertKeyvaluePair.getJSONObject(i).getString(
                        RequestKeys.KEY_INSERT_COLUMN));
                valueSet.add(insertKeyvaluePair.getJSONObject(i).getString(
                        RequestKeys.KEY_INSERT_VALUE));
            }
        }

        // Check if any required key is existed in the JSON data
        // If not, will throw JSONException
        checkKeyParameterExisted(requiredKeySet, keySet);
        ServiceLog.d(TAG, "Parsed the INSERT request.");

        return SQLHelper.buildInsertSQL(table, keySet, valueSet);

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

        ArrayList<String> constraintKeySet = new ArrayList<String>();
        ArrayList<String> constraintOperatorSet = new ArrayList<String>();
        ArrayList<String> constraintValueSet = new ArrayList<String>();

        parseConstraint(postData, constraintKeySet, constraintOperatorSet, constraintValueSet);

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

        return SQLHelper.buildQuerySQL(table, returnSet, constraintKeySet, constraintOperatorSet,
                constraintValueSet, number, orderBy, isASC);
    }

    private static String parseUpdateRequest(JSONObject postData, String table)
            throws JSONException
    {
        ArrayList<String> keySet = new ArrayList<String>();
        ArrayList<String> valueSet = new ArrayList<String>();

        JSONArray updateKeyvaluePair = postData.getJSONArray(RequestKeys.KEY_UPDATE_KEYVALUE_PAIR);

        for (int i = 0; i < updateKeyvaluePair.length(); i++)
        {
            if (updateKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_UPDATE_COLUMN)
                    && updateKeyvaluePair.getJSONObject(i).has(RequestKeys.KEY_UPDATE_VALUE))
            {
                keySet.add(updateKeyvaluePair.getJSONObject(i).getString(
                        RequestKeys.KEY_UPDATE_COLUMN));
                valueSet.add(updateKeyvaluePair.getJSONObject(i).getString(
                        RequestKeys.KEY_UPDATE_VALUE));
            }
        }

        ArrayList<String> constraintKeySet = new ArrayList<String>();
        ArrayList<String> constraintOperatorSet = new ArrayList<String>();
        ArrayList<String> constraintValueSet = new ArrayList<String>();

        parseConstraint(postData, constraintKeySet, constraintOperatorSet, constraintValueSet);

        return SQLHelper.buildUpdateSQL(table, keySet, valueSet, constraintKeySet,
                constraintOperatorSet, constraintValueSet);

    }

    private static void parseConstraint(JSONObject postData, ArrayList<String> constraintKeySet,
            ArrayList<String> constraintOperatorSet, ArrayList<String> constraintValueSet)
            throws JSONException
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
                constraintKeySet.add(data.getString(RequestKeys.KEY_CONSTRAINT_COLUMN));
                constraintOperatorSet.add(data.getString(RequestKeys.KEY_CONSTRAINT_OPERATOR));
                constraintValueSet.add(data.getString(RequestKeys.KEY_CONSTRAINT_VALUE));
            }
        }
    }

    public static void checkKeyParameterExisted(List<String> requiredParameters,
            List<String> parameters) throws JSONException
    {
        if (parameters == null || requiredParameters == null)
        {
            ServiceLog.e(TAG, "JSON data or require parameter is null.");
            return;
        }

        HashSet<String> requiredhs = new HashSet<String>();
        requiredhs.addAll(requiredParameters);
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(parameters);

        if (!hs.containsAll(requiredhs))
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
