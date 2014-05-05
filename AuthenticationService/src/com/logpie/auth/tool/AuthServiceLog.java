/*
 * Copyright (c) 2014 logpie.com
 * All rights reserved.
 */
package com.logpie.auth.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.logpie.auth.config.AuthConfig;

public class AuthServiceLog
{
    private static boolean sDebug = AuthConfig.isDebugging;
    private static String sPath = AuthConfig.LogPath;
    private static final String LOG_TAG = AuthServiceLog.class.getName();

    public static void openLog()
    {
        sDebug = true;
    }

    public static void setDebugMode(boolean isDebuging)
    {
        sDebug = isDebuging;
    }

    public static void i(String TAG, String info)
    {
        if (sDebug)
        {
            System.out.println("Info:" + TAG + " : " + info);
        }
    }

    public static void d(String TAG, String info)
    {
        if (sDebug)
        {
            System.out.println("Debug:" + TAG + " : " + info);
        }
    }

    public static void e(String TAG, String info)
    {
        if (sDebug)
        {
            System.out.println("Error:" + TAG + " : " + info);
        }
    }

    public static void logRequest(String TAG, String requestID, String info)
    {
        writeFile(requestID + TAG, info);
    }

    public synchronized static void writeFile(String TAG, String info)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss:SSS");
        // get current date time with Date()
        Date date = new Date();
        FileWriter writer = null;
        try
        {
            File file = new File(sPath, dateFormat.format(date) + ".txt");
            if (!file.exists())
                file.createNewFile();
            writer = new FileWriter(file.getAbsoluteFile(), true);
            writer.write(timeFormat.format(date) + "-->");
            writer.write(TAG + ": ");
            writer.write(info);
            writer.write(AuthConfig.changeLineCharacter);
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            AuthServiceLog.e(LOG_TAG, "<Logpie> IOException: When writing log file.");
            AuthServiceLog.e(LOG_TAG, e.getMessage());
        }
    }
}
