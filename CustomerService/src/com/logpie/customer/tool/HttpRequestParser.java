package com.logpie.customer.tool;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.customer.exception.HttpRequestIsNullException;

public class HttpRequestParser
{
    private static final String TAG = HttpRequestParser.class.getName();

    // Check JsonObject is null or not. Null means parser fails.
    /**
     * Takes in HttpServletRequest, read the postbody to json object.
     * 
     * @param request
     *            HttpRequest comes in servlet
     * @return Postbody to JSONObject
     * @throws HttpRequestIsNullException
     */
    public static JSONObject httpRequestParser(HttpServletRequest request)
    {
        if (request == null)
        {
            CustomerServiceLog.e(TAG, "The coming request is null!");
            // Return null
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        try
        {
            StringBuilder stringBuilder = new StringBuilder();

            BufferedReader reader = new BufferedReader(request.getReader());
            String line;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            jsonObj = new JSONObject(stringBuilder.toString());

        } catch (JSONException e)
        {
            CustomerServiceLog
                    .e(TAG, "JSON convert exception. Cannot convert String to JSONObject");
            CustomerServiceLog.e(TAG, e.getMessage());
            // Return null if find IO exception
            return null;
        } catch (IOException e)
        {
            CustomerServiceLog.e(TAG,
                    "HttpRequest IO exception. Cannot get httprequest inputstream.");
            CustomerServiceLog.e(TAG, e.getMessage());
            // Return null if find IO exception
            return null;
        }
        return jsonObj;
    }
}
