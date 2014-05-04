package com.logpie.auth.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.logpie.auth.logic.AuthResponseKeys;

public class HttpResponseWriter
{
    private static final String TAG = HttpResponseWriter.class.getName();

    public static void reponseWithSuccess(JSONObject returnData, HttpServletResponse response)
    {
        if (returnData != null && returnData.has(AuthResponseKeys.KEY_AUTH_RESULT))
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().print(returnData.toString());
                AuthServiceLog.d(TAG, returnData.toString());
            } catch (IOException e)
            {
                AuthServiceLog.e(TAG, e.getMessage());
                AuthServiceLog.e(TAG, "IOException when returning result");

            }
        }
        else
        {
            AuthServiceLog
                    .e(TAG,
                            "Response data must contain key: AuthResponseKeys.KEY_RESULT = com.logpie.auth.result");
        }
    }

}
