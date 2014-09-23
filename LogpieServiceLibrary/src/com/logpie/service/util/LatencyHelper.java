package com.logpie.service.util;

/**
 * Used to collect latency information
 * @author yilei
 *
 */
public class LatencyHelper
{
    private final String mOperationName;
    private final long mStartTime;
    private long mEndTime;
    private long mLatency;
    
    public LatencyHelper(final String operationName)
    {
        mOperationName = operationName; 
        mStartTime =  getCurrentTimeMillis();
    }
    
    public LatencyHelper (final String operationName,long startTime)
    {
        mOperationName = operationName; 
        mStartTime = startTime;
    }
    
    private long getCurrentTimeMillis()
    {
        return System.currentTimeMillis();
    }
    
    public void stop()
    {
        mEndTime = getCurrentTimeMillis();
        mLatency = mEndTime - mStartTime;
    }
    
    public long stopAndGetLantency()
    {
        mEndTime = getCurrentTimeMillis();
        mLatency = mEndTime - mStartTime;
        return mLatency;
    }
    
    public long getLatency()
    {
        return mLatency;
    }
    
    public String getOperationName()
    {
        return mOperationName;
    }
    
    public void logLatencyInformation(String TAG, String... explanation)
    {
        ServiceLog.i(TAG, "The operation of "+getOperationName() + " totally takes: " + mLatency + " milli seconds." + explanation.toString());
    }

}
