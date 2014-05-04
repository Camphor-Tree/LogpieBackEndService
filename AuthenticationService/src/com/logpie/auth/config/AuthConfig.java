/*
 * Copyright (c) 2014 logpie.com
 * All rights reserved.
 */
package com.logpie.auth.config;

/**
 * This should be the central configuration file. Deploy Authentication Service
 * should first configure this file.
 * 
 * @author yilei
 * 
 */
public class AuthConfig
{
    /**
     * Is debuging? When deploy, should turn this to false;
     */
    public static final boolean isDebugging = true;
    /**
     * The logfile's path
     */
    public static final String LogPath = "E:/AuthLog/";

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
