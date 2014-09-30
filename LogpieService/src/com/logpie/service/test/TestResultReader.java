package com.logpie.service.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TestResultReader
{
    private static final int TEST_TIME_INDEX = 0;
    private static final int TEST_NAME_INDEX = 1;
    private static final int TEST_RESULT_INDEX = 2;
    private static final int TEST_LATENCY_INDEX = 3;

    private static final String testResultCSVFile = "G:/workspace_logpie/LogpieBackEndService/LogpieServiceEndpointTest/TestResult/Register.csv";

    public TestResultReader()
    {
    }

    public ArrayList<String> getTestResult()
    {
        ArrayList<String> testData = new ArrayList<String>();
        // Generate the test tag (time)
        String testTag = "";
        String testResult = "";
        String testLatency = "";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try
        {

            br = new BufferedReader(new FileReader(testResultCSVFile));
            while ((line = br.readLine()) != null)
            {
                String[] singleTestResult = line.split(cvsSplitBy);

                // generate tag string
                testTag += "\"";
                testTag += singleTestResult[TEST_TIME_INDEX];
                testTag += "\",";

                // generate result string
                testResult += "\"";
                testResult += singleTestResult[TEST_RESULT_INDEX];
                testResult += "\",";

                // genrate latency string
                testLatency += "\"";
                testLatency += singleTestResult[TEST_LATENCY_INDEX];
                testLatency += "\",";
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        testData.add(testTag);
        testData.add(testResult);
        testData.add(testLatency);
        return testData;
    }
}
