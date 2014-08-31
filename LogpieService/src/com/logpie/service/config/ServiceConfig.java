package com.logpie.service.config;

public class ServiceConfig
{
    /**
     * Is debugging? When deploy, should turn this to false;
     */
    public static final boolean isDebugging = true;

    /**
     * The logfile's path on Jiahang's computer
     */
    public static final String LogPath = "/Users/xujiahang/Documents/workspace/ServiceLog/";

    /**
     * The logfile's path on Yilei's computer public static final String LogPath
     * = "E:/ServiceLog/";
     */

    /**
     * Different system may have different changeLine Character.
     * 
     * Windows: \r\n
     * 
     * Linux/Unix/OSX: \n
     */
    public static final String changeLineCharacter = "\r\n";

    /**
     * The postgresSQL database information
     */
    public static final String PostgreSQL_URL = "jdbc:postgresql://localhost:5432/logpie";
    public static final String PostgreSQL_Username = "postgres";
    public static final String PostgreSQL_Password = "123456";
}
