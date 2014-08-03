package com.logpie.customer.data;

public class SQLHelper
{
    public static final String SCHEMA_UID = "uid";
    public static final String SCHEMA_EMAIL = "email";
    public static final String SCHEMA_NICKNAME = "nickname";
    public static final String SCHEMA_GENDER = "gender";
    public static final String SCHEMA_BIRTHDAY = "birthday";
    public static final String SCHEMA_CITY = "city";
    public static final String SCHEMA_country = "country";
    public static final String SCHEMA_LASTUPDATEDTIME = "lastupdatedtime";
    public static final String SCHEMA_ISORGANIZATION = "isorganization";
    
    private static final String TAG = SQLHelper.class.getName();
    public static final String SQL_CREATE_USER_TABLE = "CREATE TABLE \"user\" (uid integer NOT NULL, email character varying NOT NULL, nickname character(18) NOT NULL, birthday date, city integer, country character varying, gender boolean NOT NULL DEFAULT true,last_updated_time timestamp with time zone NOT NULL DEFAULT now(), is_organization boolean NOT NULL DEFAULT false, CONSTRAINT \"PK_user\" PRIMARY KEY (uid) USING INDEX TABLESPACE pg_default) WITH (OIDS = FALSE) TABLESPACE pg_default; ALTER TABLE \"user\" OWNER TO postgres;";
    
    public static String buildInsertSQL(String uid, String email, String nickname)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("insert into user (uid, email, nickname) values (\'");
        sqlBuilder.append(uid);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(email);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(nickname);
        sqlBuilder.append("\'");
        
        return sqlBuilder.toString();
    }

    public static String buildFindSQL(String key, String value)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from user where ");
        sqlBuilder.append(key);
        sqlBuilder.append(" =\'");
        sqlBuilder.append(value);
        sqlBuilder.append("\';");
        return sqlBuilder.toString();
    }
    
}
