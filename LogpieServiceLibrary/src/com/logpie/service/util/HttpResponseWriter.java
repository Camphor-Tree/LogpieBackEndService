package com.logpie.service.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * The Class is help to write response data to client
 * 
 * @author yilei
 *
 */
public class HttpResponseWriter
{
    private static final String TAG = HttpResponseWriter.class.getName();

    /**
     * This API will check whether your response json contains the result_key
     * You must specify the result key.
     * Each Service may different.
     * 
     * @param serviceSpecificResultKey
     * @param returnData
     * @param response
     */
    public static void reponseWithSuccess(String serviceSpecificResultKey, JSONObject returnData, HttpServletResponse response)
    {
        if (returnData != null && returnData.has(serviceSpecificResultKey))
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().print(returnData.toString());
                ServiceLog.d(TAG, returnData.toString());
            } catch (IOException e)
            {         
                ServiceLog.e(TAG, "IOException when returning result", e);

            }
        }
        else
        {
            ServiceLog.e(TAG, "Response data must contain a result_key. Each Service may behave different.");
        }
    }
}
