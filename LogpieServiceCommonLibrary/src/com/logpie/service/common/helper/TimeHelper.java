package com.logpie.service.common.helper;

import java.sql.Timestamp;
import java.util.Date;

public class TimeHelper {
	public static Timestamp getCurrentTimestamp()
	{
		Date now = new Date();
		return new Timestamp(now.getTime());
	}
}
