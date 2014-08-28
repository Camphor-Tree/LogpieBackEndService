package com.logpie.service.common.helper;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.service.common.error.HttpRequestIsNullException;

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
    public static JSONObject httpRequestParser(HttpServletRequest request) throws HttpRequestIsNullException
    {
        if (request == null)
        {
            throw new HttpRequestIsNullException();
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
            CommonServiceLog.e(TAG, "JSON convert exception. Cannot convert String to JSONObject", e);
            // Return null if find IO exception
            return null;
        } catch (IOException e)
        {
            CommonServiceLog.e(TAG, "HttpRequest IO exception. Cannot get httprequest inputstream.", e);
            // Return null if find IO exception
            return null;
        }
        return jsonObj;
    }
}
