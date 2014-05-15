/*
 * Copyright (c) 2014 logpie.com
 * All rights reserved.
 */
package com.logpie.service.common.helper;

/**
 * This should be the central configuration file. Deploy common Service
 * should first configure this file.
 * 
 * @author yilei
 * 
 */
public class CommonServiceConfig
{
    /**
     * Is debuging? When deploy, should turn this to false;
     */
    public static final boolean isDebugging = true;


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
