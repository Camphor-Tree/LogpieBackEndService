package com.logpie.customer.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.logpie.cutomer.logic.CustomerResponseKeys;

public class HttpResponseWriter
{
    private static final String TAG = HttpResponseWriter.class.getName();

    public static void reponseWithSuccess(JSONObject returnData, HttpServletResponse response)
    {
        if (returnData != null && returnData.has(CustomerResponseKeys.KEY_AUTH_RESULT))
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().print(returnData.toString());
                CustomerServiceLog.d(TAG, returnData.toString());
            } catch (IOException e)
            {
                CustomerServiceLog.e(TAG, e.getMessage());
                CustomerServiceLog.e(TAG, "IOException when returning result");

            }
        }
        else
        {
            CustomerServiceLog
                    .e(TAG,
                            "Response data must contain key: AuthResponseKeys.KEY_RESULT = com.logpie.auth.result");
        }
    }
}
