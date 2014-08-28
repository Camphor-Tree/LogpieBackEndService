package com.logpie.service.data;

import java.util.ArrayList;

import com.logpie.service.common.helper.CommonServiceLog;

public class SQLHelper
{
    public static final String SCHEMA_UID = "uid";
    public static final String SCHEMA_EMAIL = "email";
    public static final String SCHEMA_NICKNAME = "nickname";
    public static final String SCHEMA_GENDER = "gender";
    public static final String SCHEMA_BIRTHDAY = "birthday";
    public static final String SCHEMA_CITY = "city";
    public static final String SCHEMA_COUNTRY = "country";
    public static final String SCHEMA_LASTUPDATEDTIME = "lastupdatedtime";
    public static final String SCHEMA_ISORGANIZATION = "isorganization";
    
    private static final String TAG = SQLHelper.class.getName();
    
    /**
     * SQL syntax to create tables
     */
    public static final String SQL_CREATE_USER_TABLE = "CREATE TABLE \"user\" (uid integer NOT NULL, email character varying NOT NULL, nickname character(18) NOT NULL, birthday date, city character varying, country character varying, gender boolean NOT NULL DEFAULT true,last_updated_time timestamp with time zone NOT NULL DEFAULT now(), is_organization boolean NOT NULL DEFAULT false, CONSTRAINT \"PK_user\" PRIMARY KEY (uid) USING INDEX TABLESPACE pg_default) WITH (OIDS = FALSE) TABLESPACE pg_default; ALTER TABLE \"user\" OWNER TO postgres;";
    
    
    public static String buildRegisterSQL(String uid, String email, String nickname, String city)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        CommonServiceLog.d(TAG, "Building sql of INSERT request...");
        sqlBuilder
                .append("insert into \"user\" (uid, email, nickname, city) values (\'");
        sqlBuilder.append(uid);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(email);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(nickname);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(city);
        sqlBuilder.append("\')");
        
        return sqlBuilder.toString();
    }

    public static String buildQuerySQL(ArrayList<String> key_set, String constraint_key, String constraint_value)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        CommonServiceLog.d(TAG, "Building sql of QUERY request...");
        if(key_set==null)
        {
        	sqlBuilder.append("select * ");
        }else
        {
        	CommonServiceLog.d(TAG, "Parsing the JSONArray of QUERY request to build sql...");
        	sqlBuilder.append("select ");
        	for(int i=0;i<key_set.size();i++)
        	{
        		sqlBuilder.append(key_set.get(i));
        		if(i==key_set.size()-1)
        		{
        			sqlBuilder.append(" ");
        		}else
        		{
        			sqlBuilder.append(", ");
        		}       		
        	}
        	
        }
        sqlBuilder.append("from user where ");
        sqlBuilder.append(constraint_key);
        sqlBuilder.append(" like \'");
        sqlBuilder.append(constraint_value);
        sqlBuilder.append("\'");
        return sqlBuilder.toString();
    }
    
    public static String buildUpdateSQL(ArrayList<String> key_set, ArrayList<String> value_set, String constraint_key, String constraint_value)
    {
    	StringBuilder sqlBuilder = new StringBuilder();
    	CommonServiceLog.d(TAG, "Building sql of UPDATE request...");
    	if(key_set.size()!=value_set.size())
    	{
    		CommonServiceLog.e(TAG, "The length of keySet is not the same as valueSet when parsing the JSONArray of UPDATE request.");
    		return null;
    	}else
    	{
    		CommonServiceLog.d(TAG, "Parsing the JSONArray of UPDATE request to build sql...");
    		for(int i=0;i<key_set.size();i++)
    		{
    			String key = key_set.get(i);
    			String value = value_set.get(i);
    			sqlBuilder.append("update user set ");
                sqlBuilder.append(key);
                sqlBuilder.append(" = \'");
                sqlBuilder.append(value);
                if(i==key_set.size()-1)
        		{
        			sqlBuilder.append("\' ");
        		}else
        		{
        			sqlBuilder.append("\', ");
        		}
    		}	            
    	}       
        sqlBuilder.append("where ");
        sqlBuilder.append(constraint_key);
        sqlBuilder.append(" like \'");
        sqlBuilder.append(constraint_value);
        sqlBuilder.append("\'");
        return sqlBuilder.toString();
    }
    
}
