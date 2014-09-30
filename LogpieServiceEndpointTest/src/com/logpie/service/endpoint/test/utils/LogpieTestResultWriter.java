package com.logpie.service.endpoint.test.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.logpie.service.util.ServiceLog;

/**
 * This class used to write test result into file
 * 
 * @author yilei
 * 
 */
public class LogpieTestResultWriter
{
    private static final String dataResult_path = "./TestResult/";
    private static final String dataResult_file_format = ".csv";
    private static final String TAG = LogpieTestResultWriter.class.getName();

    public static void writeResult(String testName, String resultString)
    {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dataResult_path
                + testName + dataResult_file_format, true))))
        {
            out.println(resultString);
        } catch (IOException e)
        {
            ServiceLog.e(TAG, "Error happened when trying to write result record");
            return;
        }
    }

    public static String buildResultRecord(String time, String testName, String testResult,
            String testLatency)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(time);
        stringBuilder.append(",");
        stringBuilder.append(testName);
        stringBuilder.append(",");
        stringBuilder.append(testResult);
        stringBuilder.append(",");
        stringBuilder.append(testLatency);
        return stringBuilder.toString();

    }

}
