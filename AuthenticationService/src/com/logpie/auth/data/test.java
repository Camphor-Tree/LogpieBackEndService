package com.logpie.auth.data;

import org.json.JSONException;

public class test
{

    /**
     * @param args
     * @throws JSONException
     */
    public static void main(String[] args) throws JSONException
    {
        System.out.println(SQLHelper.buildLoginSQL("yilei@aa.com", "123456"));

    }

}
