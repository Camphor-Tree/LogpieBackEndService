package com.logpie.customer.config;

public class CustomerConfig
{
    /**
     * Is debugging? When deploy, should turn this to false;
     */
    public static final boolean isDebugging = true;
    /**
     * The logfile's path
     */
    public static final String LogPath = "E:/CustomerLog/";

    /**
     * Different system may have different changeLine Character.
     * 
     * Windows: \r\n
     * 
     * Linux/Unix/OSX: \n
     */
    public static final String changeLineCharacter = "\r\n";

    public static final String PostgreSQL_URL = "jdbc:postgresql://localhost:5432/logpie";
    public static final String PostgreSQL_Username = "postgres";
    public static final String PostgreSQL_Password = "123456";
}
