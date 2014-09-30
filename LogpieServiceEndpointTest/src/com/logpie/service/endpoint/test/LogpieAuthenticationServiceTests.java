package com.logpie.service.endpoint.test;

import java.util.UUID;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logpie.service.connection.EndPoint.ServiceURL;
import com.logpie.service.connection.GenericConnection;
import com.logpie.service.endpoint.test.utils.LogpieTestResultWriter;
import com.logpie.service.util.RequestKeys;
import com.logpie.service.util.ResponseKeys;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;
import com.logpie.service.util.TimeHelper;

/**
 * This test is used to hit AuthenticationService end point to do the functional
 * test. The result should be write into the csv file.
 * 
 * 
 * AuthenticationService is the service handle authentication related logic.
 * Currently, the authentication service support: 1. login 2. register 3. token
 * validation 4. token exchange 5. reset password.
 * 
 * @author yilei
 * 
 */
public class LogpieAuthenticationServiceTests extends TestCase
{
    private static final String TAG = LogpieAuthenticationServiceTests.class.getName();
    private GenericConnection mConnection;
    private boolean mTestResult;
    private String mCurrentRunningTest;
    private long mStartTime;
    private long mEndTime;

    @Before
    public void setUp() throws Exception
    {
        // set up connection
        mConnection = new GenericConnection();
        mConnection.initialize(ServiceURL.AuthenticationService);
        ServiceLog
                .i(TAG,
                        "===========================================Starting Test...===========================================");
        mStartTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() throws Exception
    {
        long latency = handleAndRecordTestResult();
        ServiceLog
                .i(TAG,
                        "===========================================Ending Test...=========================================== \n Tearing down... Test:"
                                + mCurrentRunningTest
                                + " is done! The latency for this test is :"
                                + latency + " ms" + " The test Result is:" + mTestResult);
        mCurrentRunningTest = null;
    }

    /**
     * Test for register
     */
    // @Test
    public void testRegister()
    {
        mCurrentRunningTest = "Register";
        mStartTime = System.currentTimeMillis();

        JSONObject registerData = new JSONObject();
        try
        {
            registerData.put(RequestKeys.KEY_REQUEST_TYPE, "REGISTER");

            registerData.put(RequestKeys.KEY_EMAIL, UUID.randomUUID().toString().substring(0, 10)
                    + "@logpie.com");
            registerData.put(RequestKeys.KEY_PASSWORD, "123456");
            registerData.put(RequestKeys.KEY_NICKNAME, "LogpieTester");
            registerData.put(RequestKeys.KEY_CITY, "Seattle");
            registerData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            ServiceLog.i(TAG, "Register String" + registerData.toString());
            mConnection.setRequestData(registerData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        ServiceCallback callback = getTestCallback();
        this.mConnection.send(callback);
        assertTrue(mTestResult);

    }

    /**
     * Test for authenticate
     */
    // @Test
    public void testAuthenticateSuccess()
    {
        mCurrentRunningTest = "Authenticate";
        mStartTime = System.currentTimeMillis();

        JSONObject authenticateData = new JSONObject();
        try
        {
            authenticateData.put(RequestKeys.KEY_REQUEST_TYPE, "AUTHENTICATE");
            authenticateData.put(RequestKeys.KEY_EMAIL, "a7197901-e@logpie.com");
            authenticateData.put(RequestKeys.KEY_PASSWORD, "123456");
            authenticateData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            ServiceLog.i(TAG, "Register String" + authenticateData.toString());
            mConnection.setRequestData(authenticateData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        ServiceCallback callback = getTestCallback();
        this.mConnection.send(callback);
        assertTrue(mTestResult);
    }

    /**
     * Test for reset Password
     */
    @Test
    public void testResetPasswordSuccess()
    {
        mCurrentRunningTest = "ResetPassword";
        mStartTime = System.currentTimeMillis();

        JSONObject resetPasswordData = new JSONObject();
        try
        {
            resetPasswordData.put(RequestKeys.KEY_REQUEST_TYPE, "RESET_PASSWORD");
            resetPasswordData.put(RequestKeys.KEY_EMAIL, "a7197901-e@logpie.com");
            resetPasswordData.put(RequestKeys.KEY_PASSWORD, "123456");
            resetPasswordData.put(RequestKeys.KEY_NEW_PASSWORD, "123456");
            resetPasswordData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            ServiceLog.i(TAG, "Register String" + resetPasswordData.toString());
            mConnection.setRequestData(resetPasswordData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        ServiceCallback callback = getTestCallback();
        this.mConnection.send(callback);
        assertTrue(mTestResult);
    }

    /**
     * Test for Token Exchange
     */
    @Test
    public void testTokenExchange()
    {
        mCurrentRunningTest = "TOKEN_EXCHANGE";
        mStartTime = System.currentTimeMillis();

        JSONObject tokenExchangeData = new JSONObject();
        try
        {
            tokenExchangeData.put(RequestKeys.KEY_REQUEST_TYPE, "TOKEN_EXCHANGE");
            tokenExchangeData.put(RequestKeys.KEY_DECLARE_UID, "30");
            tokenExchangeData
                    .put(RequestKeys.KEY_ACCESS_TOKEN,
                            "MW/jZvnMKADVknhsgnCco1WvaIDtlxQanSkk3r6sMM1gn1oWMHVAbANXM93wTytu8VLnOKpbwDXTeSesU68zsCJtSvvtL8XZy/WCv3kmfBCpJVJee16OQfqG2anmEWL1HopquryfkM3Od25fX0ETq+1fefaQ9r9BJ1Jyhq5C2/Q=");
            tokenExchangeData
                    .put(RequestKeys.KEY_REFRESH_TOKEN,
                            "IcAwU9P/WMas18GWU8J5DYlYah7Hyy+IZZX+WeeDaeQKXlk/sV8ZUjyuBFuwp6KblaV89hnXsWexnM6EtY7+GJe+o2/v2oXiRdCwCXDouBkG3didCXnYyW9/0xLWhvMXpPpUDMoQVzH6cqEMWjCTjXh/WZkICKQMMgkkvUT83Dc=");
            tokenExchangeData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            ServiceLog.i(TAG, "Register String" + tokenExchangeData.toString());
            mConnection.setRequestData(tokenExchangeData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        ServiceCallback callback = getTestCallback();
        this.mConnection.send(callback);
        assertTrue(mTestResult);
    }

    /**
     * Test for Token Validation
     */
    @Test
    public void testTokenValidation()
    {
        mCurrentRunningTest = "TOKEN_VALIDATION";
        // Do an authentication and get new access_token first
        JSONObject authenticateData = new JSONObject();
        try
        {
            authenticateData.put(RequestKeys.KEY_REQUEST_TYPE, "AUTHENTICATE");
            authenticateData.put(RequestKeys.KEY_EMAIL, "a7197901-e@logpie.com");
            authenticateData.put(RequestKeys.KEY_PASSWORD, "123456");
            authenticateData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            ServiceLog.i(TAG, "Register String" + authenticateData.toString());
            mConnection.setRequestData(authenticateData);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        ServiceCallback authenticateCallback = new ServiceCallback()
        {

            @Override
            public void onSuccess(JSONObject result)
            {
                String access_token = null;
                try
                {
                    access_token = result.getJSONObject(GenericConnection.KEY_RESPONSE_DATA)
                            .getString(ResponseKeys.KEY_ACCESS_TOKEN);
                } catch (JSONException e)
                {
                    fail("JSONException when trying to get a new access_token");
                }

                // Refresh the connection object.
                mConnection.initialize(ServiceURL.AuthenticationService);
                mStartTime = System.currentTimeMillis();
                JSONObject tokenValidationData = new JSONObject();
                try
                {
                    tokenValidationData.put(RequestKeys.KEY_REQUEST_TYPE, "TOKEN_VALIDATION");
                    tokenValidationData.put(RequestKeys.KEY_DECLARE_UID, "30");
                    if (access_token == null)
                    {
                        access_token = "MW/jZvnMKADVknhsgnCco1WvaIDtlxQanSkk3r6sMM1gn1oWMHVAbANXM93wTytu8VLnOKpbwDXTeSesU68zsCJtSvvtL8XZy/WCv3kmfBCpJVJee16OQfqG2anmEWL1HopquryfkM3Od25fX0ETq+1fefaQ9r9BJ1Jyhq5C2/Q=";
                    }
                    tokenValidationData.put(RequestKeys.KEY_TOKEN, access_token);
                    tokenValidationData.put(RequestKeys.KEY_TOKEN_TYPE, "access_token");

                    tokenValidationData.put(RequestKeys.KEY_ACCESS_SERVICE, "LogpieService");
                    ServiceLog.i(TAG, "Register String" + tokenValidationData.toString());

                    mConnection.setRequestData(tokenValidationData);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                ServiceCallback callback = getTestCallback();
                mConnection.send(callback);
                assertTrue(mTestResult);
            }

            @Override
            public void onError(JSONObject errorMessage)
            {

            }
        };
        this.mConnection.send(authenticateCallback);

    }

    private long handleAndRecordTestResult()
    {
        // write test result
        mEndTime = System.currentTimeMillis();
        long latency = mEndTime - mStartTime;
        String time = TimeHelper.getCurrentTimestamp().toString();
        String result;
        if (mTestResult)
        {
            result = "1";
        }
        else
        {
            result = "0";
        }
        String resultRecord = LogpieTestResultWriter.buildResultRecord(time, mCurrentRunningTest,
                result, Long.toString(latency));
        LogpieTestResultWriter.writeResult(mCurrentRunningTest, resultRecord);
        return latency;
    }

    private ServiceCallback getTestCallback()
    {
        ServiceCallback callback = new ServiceCallback()
        {

            @Override
            public void onSuccess(JSONObject result)
            {
                ServiceLog.i(TAG, "Register success: " + result.toString());
                mTestResult = true;

            }

            @Override
            public void onError(JSONObject errorMessage)
            {
                String errorString = "Error when requesting logpie authenticate service.  Error message is: "
                        + errorMessage.toString();

                ServiceLog.i(TAG, errorString);
                mTestResult = false;
                fail(errorString);

            }
        };
        return callback;
    }

}
