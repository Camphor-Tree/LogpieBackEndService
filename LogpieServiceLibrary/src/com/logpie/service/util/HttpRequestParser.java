package com.logpie.service.util;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.error.HttpRequestIsNullException;

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
            throws HttpRequestIsNullException
    {
        if (request == null)
        {
            throw new HttpRequestIsNullException();
        }

        JSONObject jsonObj = new JSONObject();
        try
        {
            StringBuilder stringBuilder = new StringBuilder();

            // Must set chracter encoding as UTF-8 to get the correct characters
            request.setCharacterEncoding("UTF-8");

            BufferedReader reader = new BufferedReader(request.getReader());
            String line;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            jsonObj = new JSONObject(stringBuilder.toString());

        } catch (JSONException e)
        {
            ServiceLog.e(TAG,
                    "JSON convert exception. Cannot convert String to JSONObject", e);
            // Return null if find IO exception
            return null;
        } catch (IOException e)
        {
            ServiceLog.e(TAG,
                    "HttpRequest IO exception. Cannot get httprequest inputstream.", e);
            // Return null if find IO exception
            return null;
        }
        return jsonObj;
    }
}
