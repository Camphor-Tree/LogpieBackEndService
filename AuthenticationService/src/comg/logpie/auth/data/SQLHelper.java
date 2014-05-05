package comg.logpie.auth.data;

import java.util.List;

import com.logpie.auth.logic.TokenGenerator;
import com.logpie.auth.logic.TokenScopeManager;
import com.logpie.auth.logic.TokenScopeManager.Scope;
import com.logpie.auth.tool.AuthServiceLog;

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

    public static String buildCreateUserSQL(String email, String password, List<Scope> scopes)
    {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("insert into user_auth (email,password,access_token,access_token_expire_time,refresh_token,refresh_token_expire_time) values (\'");
        sqlBuilder.append(email);
        sqlBuilder.append("\',\'");
        sqlBuilder.append(password);
        sqlBuilder.append("\',\'");
        String access_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateBaseKeySource(email, password), scopes);
        AuthServiceLog.d(TAG, "access_keysource:" + access_keysource);
        String access_token = TokenGenerator.generateToken(access_keysource);
        sqlBuilder.append(access_token);
        sqlBuilder.append("\',now() + interval \'1 hour\',\'");
        String refresh_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateBaseKeySource(email, password), scopes);
        String refresh_token = TokenGenerator.generateToken(refresh_keysource);
        sqlBuilder.append(refresh_token);
        sqlBuilder.append("\',now() + interval \'365 days\');");
        return sqlBuilder.toString();
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

    public static String buildUpdateTokenSQL(String uid, String email, String password, String token)

    {
        List<Scope> scopeList = TokenScopeManager.getScope(token);
        String access_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateBaseKeySource(email, password), scopeList);
        String new_access_token = TokenGenerator.generateToken(access_keysource);
        String refresh_keysource = TokenScopeManager.addScope(
                TokenGenerator.generateBaseKeySource(email, password), scopeList);
        String new_refresh_token = TokenGenerator.generateToken(refresh_keysource);

        String sql = String
                .format("update user_auth set access_token = \'%s\', access_token_expire_time = now() + interval \'1 hour\', refresh_token = \'%s\', refresh_token_expire_time = now() + interval \'365 days\' where uid = \'%s\';",
                        new_access_token, new_refresh_token, uid);

        return sql;
    }
}
