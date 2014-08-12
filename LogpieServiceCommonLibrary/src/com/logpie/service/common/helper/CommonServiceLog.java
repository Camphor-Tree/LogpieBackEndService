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
    private static final String BREAK_LINE = "\n--------------------------------------";

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

    /* 
     * Without requestID
     */
    public static void i(String TAG, String info)
    {
        if (sDebug)
        {
            System.out.println(getTimeStamp () + " Info: " + TAG + " : " + info + BREAK_LINE);
        }
    }

    public static void d(String TAG, String info)
    {
        if (sDebug)
        {
            System.out.println(getTimeStamp () + " Debug: " + TAG + " : " + info + BREAK_LINE);
        }
    }

    public static void e(String TAG, String info)
    {
    	if (sDebug)
        {
            System.out.println(getTimeStamp () + " Error: " + TAG + " : " + info + BREAK_LINE);
        }
    }
    
    public static void e(String TAG, String info, Exception e)
    {
        if (sDebug)
        {
            String time = getTimeStamp ();
            e.printStackTrace();
            System.out.println(time + " | Error: " + TAG + " : " + info + BREAK_LINE);
        }
    }
    
    /* 
     * With requestID
     */
    public static void i(String TAG, String info, String requestID)
    {
        if (sDebug)
        {
            System.out.println(getTimeStamp() + " <-> Request ID:" + requestID + " | Info: " + TAG + " : " + info + BREAK_LINE);
        }
    }

    public static void d(String TAG, String info, String requestID)
    {
        if (sDebug)
        {
            System.out.println(getTimeStamp() + " <-> Request ID:" + requestID + " | Debug: " + TAG + " : " + info + BREAK_LINE);
        }
    }
    
    public static void e(String TAG, String info, String requestID)
    {
    	if (sDebug)
        {
            System.out.println(getTimeStamp() + " <-> Request ID:" + requestID + " | Error: " + TAG + " : " + info + BREAK_LINE);
        }
    }
    /**
     * Sample log:
     * 2014/08/11-09:36:47:591 Request ID:11111 | Error: a : nihao
     * 2014/08/11-09:36:47:591 Exception stacktrace:
     * Request ID:11111com.logpie.service.common.helper.sss.main(sss.java:12)
     * @param TAG
     * @param info
     * @param requestID
     * @param e
     */
    public static void e(String TAG, String info, String requestID, Exception e)
    {
    	if (sDebug)
        {
    	    String time = getTimeStamp ();
    		StringBuilder stringBuilder = new StringBuilder();
        	StackTraceElement[] stackTraceElements = e.getStackTrace();
        	for(StackTraceElement element : stackTraceElements)
        	{
        	    stringBuilder.append("Request ID: " + requestID + " | " + element.toString() + "\n");
        	}
        	System.out.println(time + " Request ID:" + requestID + " | Error: " + TAG + " : " + info);
        	System.out.println(time + " Exception stacktrace:\n" + stringBuilder.toString());
        }
    }
    
    public static void logRequest(String TAG, String requestID, String info)
    {
        writeFile(requestID + TAG, info);
    }
    
    private static String getTimeStamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss:SSS");
        // get current date time with Date()
        Date date = new Date();
        String currentTimeStamp = new String();
        currentTimeStamp =  dateFormat.format(date);
        return currentTimeStamp;
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
            CommonServiceLog.e(LOG_TAG, "<Logpie> IOException: When writing log file.",e);
        }
    }
}
