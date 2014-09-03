package com.logpie.service.common.helper;

import java.sql.Timestamp;
import java.util.Date;

public class TimeHelper
{
    private static final String TAG = TimeHelper.class.getName();

    public static Timestamp getCurrentTimestamp()
    {
        Date now = new Date();
        return new Timestamp(now.getTime());
    }

    public static Timestamp getTimestamp(String timeString)
    {
        try
        {
            return Timestamp.valueOf(timeString);
        } catch (IllegalArgumentException e)
        {
            CommonServiceLog.e(TAG,
                    "IllegalArgumentException happened when transfer string to timestamp.", e);
            return null;
        }
    }
}
