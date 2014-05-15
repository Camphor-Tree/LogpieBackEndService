/*
 * Copyright (c) 2014 logpie.com
 * All rights reserved.
 */
package com.logpie.service.common.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CommonServiceLog
{
    private static boolean sDebug = CommonServiceConfig.isDebugging;
    private static String sPath;
    private static final String LOG_TAG = CommonServiceLog.class.getName();

    public static void setLogFilePath(String path)
    {
    	sPath = path;
    }
    
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
            writer.write(CommonServiceConfig.changeLineCharacter);
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            CommonServiceLog.e(LOG_TAG, "<Logpie> IOException: When writing log file.");
            CommonServiceLog.e(LOG_TAG, e.getMessage());
        }
    }
}
