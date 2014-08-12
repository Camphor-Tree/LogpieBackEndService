package com.logpie.auth.data;

import java.util.ArrayList;
import java.util.List;

import com.logpie.auth.data.AuthDataManager.DataCallback;
import com.logpie.auth.logic.TokenGenerator;
import com.logpie.auth.logic.TokenScopeManager;
import com.logpie.auth.logic.TokenScopeManager.Scope;
import com.logpie.service.common.helper.CommonServiceLog;

public class SQLHelper
{
    public static final String SCHEMA_UID = "uid";
    public static final String SCHEMA_EMAIL = "email";
    public static final String SCHEMA_PASSWORD = "password";
    public static final String SCHEMA_ACCESS_TOKEN = "access_token";
    public static final String SCHEMA_ACCESS_TOKEN_EXPIRATION = "access_token_expire_time";
    public static final String SCHEMA_REFRESH_TOKEN = "refresh_token";
    public static final String SCHEMA_REFRESH_TOKEN_EXPIRATION = "refresh_token_expire_time";

    private static final String TAG = SQLHelper.class.getName();

    public static String buildCheckUserDuplicateSQL(String email)
    {
    	StringBuilder sqlBuilder = new StringBuilder();
    	sqlBuilder.append("select * from user_auth where email=\'");
    	sqlBuilder.append(email);
    	sqlBuilder.append("\';");
    	return sqlBuilder.toString();
    }
    
    public static String buildCreateUserStep1SQL(String email, String password)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("insert into user_auth (email,password) values (\'");
        sqlBuilder.append(email);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(password);
        sqlBuilder.append("\');");
        return sqlBuilder.toString();
    }
    
    public static ArrayList<String> buildCreateUserStep2SQL(String uid, List<Scope> scopes)
    {
    	ArrayList<String> result = new ArrayList<String>();
        String access_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateAccessTokenBaseKeySource(uid),scopes);
        CommonServiceLog.d(TAG, "access_keysource:" + access_keysource);
        String access_token = TokenGenerator.generateToken(access_keysource);
        String refresh_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateRefreshTokenBaseKeySource(),scopes);
        CommonServiceLog.d(TAG, "refresh_keysource:" + refresh_keysource);
        String refresh_token = TokenGenerator.generateToken(refresh_keysource);
        // build the sql to update the tokens
        String sql = String.format("update user_auth set access_token = '%s', access_token_expire_time = now()+ interval '1hour', refresh_token = '%s', refresh_token_expire_time = now() + interval '365days' where uid = %s;",access_token,refresh_token,uid);
        result.add(sql);
        result.add(access_token);
        result.add(refresh_token);
        return result;
    }

    public static String buildLoginSQL(String email, String password)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from user_auth where email=\'");
        sqlBuilder.append(email);
        sqlBuilder.append("\' AND password=\'");
        sqlBuilder.append(password);
        sqlBuilder.append("\';");
        return sqlBuilder.toString();
    }

    public static String buildUpdateTokenSQL(String uid, String token)
    {
        List<Scope> scopeList = TokenScopeManager.getScope(token);
        String access_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateAccessTokenBaseKeySource(uid), scopeList);
        String new_access_token = TokenGenerator.generateToken(access_keysource);
        String refresh_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateRefreshTokenBaseKeySource(), scopeList);
        String new_refresh_token = TokenGenerator.generateToken(refresh_keysource);

        String sql = String
                .format("update user_auth set access_token = \'%s\', access_token_expire_time = now() + interval \'1 hour\', refresh_token = \'%s\', refresh_token_expire_time = now() + interval \'365 days\' where uid = \'%s\';",
                        new_access_token, new_refresh_token, uid);

        return sql;
    }
    
    /**
     * Build update access_token SQL
     * @param uid
     * @param token
     * @return ArrayList<String> the first element is the sql, and the second element is access_token
     */
    public static List<String> buildUpdateAccessTokenSQL(String uid, String token)
    {
        ArrayList<String> result = new ArrayList<String>();
        List<Scope> scopeList = TokenScopeManager.getScope(token);
        String access_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateAccessTokenBaseKeySource(uid), scopeList);
        CommonServiceLog.d(TAG, "new access_token's key_source:"+access_keysource);
        String new_access_token = TokenGenerator.generateToken(access_keysource);
        CommonServiceLog.d(TAG, "new access_token:"+new_access_token);
        String sql = String
                .format("update user_auth set access_token = \'%s\', access_token_expire_time = now() + interval \'1 hour\' where uid = \'%s\';",
                        new_access_token, uid);
        result.add(sql);
        result.add(new_access_token);
        return result;
    }
    
    /**
     * Build update refresh_token SQL 
     * @param uid
     * @param token
     * @return ArrayList<String> the first element is the sql, and the second element is refresh_token
     */
    public static List<String> buildUpdateRefreshTokenSQL(String uid, String token)
    {
        ArrayList<String> result = new ArrayList<String>();
        List<Scope> scopeList = TokenScopeManager.getScope(token);
        String refresh_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateRefreshTokenBaseKeySource(), scopeList);
        String new_refresh_token = TokenGenerator.generateToken(refresh_keysource);
        String sql = String
                .format("update user_auth set refresh_token = \'%s\', refresh_token_expire_time = now() + interval \'365 days\' where uid = \'%s\';",
                        new_refresh_token, uid);
        result.add(sql);
        result.add(new_refresh_token);
        return result;
    }
}
