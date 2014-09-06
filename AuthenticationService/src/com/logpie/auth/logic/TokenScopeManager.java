package com.logpie.auth.logic;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.logpie.service.common.helper.CommonServiceLog;

/**
 * Every token should have a scope. TokenScopeManager is used to manager and
 * valid the token's scope.
 * 
 * @author yilei
 * 
 */
public class TokenScopeManager
{
    private static final String TAG = TokenScopeManager.class.getName();

    public static enum Scope
    {
        RocketService, LogpieService, AuthenticationService
    }

    public static String addScope(String keySource, List<Scope> scopes)
    {
        if (keySource != null)
        {
            StringBuilder stringBuilder = new StringBuilder(keySource);
            for (Scope scope : scopes)
            {
                stringBuilder.append("$");
                stringBuilder.append(scope.toString());
            }
            return stringBuilder.toString();
        }
        else
        {
            CommonServiceLog.e(TAG, "The keySource cannot be null!");
            return null;
        }

    }

    public static List<Scope> getScope(String token)
    {
        List<Scope> scopesList = new LinkedList<Scope>();
        String decodeToken = TokenGenerator.decodeToken(token);
        String scopeString = decodeToken.substring(decodeToken.indexOf("$") + 1,
                decodeToken.length());
        String[] scopes = scopeString.split("\\$");
        for (String scope : scopes)
        {
            scopesList.add(Scope.valueOf(scope));
        }
        return scopesList;
    }

    /**
     * Check whether the scope is in token.
     * 
     * @param token
     * @param checkScope
     * @return true, if the scope in contained in the token
     */
    public static boolean isScopeValid(String token, Scope checkScope)
    {
        String scopeString = token.substring(token.indexOf("$") + 1, token.length());
        String[] scopes = scopeString.split("$");
        for (String scope : scopes)
        {
            if (checkScope.equals(Scope.valueOf(scope)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param token
     * @param checkScopeList
     * @return true, if all the scopes in list exist
     */
    public static boolean isScopesValid(String token, List<Scope> checkScopeList)
    {
        HashSet<Scope> scopeSet = new HashSet<Scope>();
        String scopeString = token.substring(token.indexOf("$") + 1, token.length());
        String[] scopes = scopeString.split("$");
        for (String scope : scopes)
        {
            scopeSet.add(Scope.valueOf(scope));
        }

        for (Scope checkScope : checkScopeList)
        {
            if (!scopeSet.contains(checkScope))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Build the scopes for new user. The token would contain the information
     * about whether the user can access specific logpie service
     * 
     * @return list of the scopes, new user can access to
     */
    public static List<Scope> buildNewUserScope()
    {
        List<Scope> scopes = new LinkedList<Scope>();
        scopes.add(Scope.RocketService);
        scopes.add(Scope.LogpieService);
        scopes.add(Scope.AuthenticationService);
        return scopes;
    }
}
