package com.logpie.service.authentication;

public class AuthenticationError
{
    // Token is invalid, may be from attacker
    public static String ERROR_TOKEN_INVALID = "token invalid";
    // Token get expired
    public static String ERROR_TOKEN_EXPIRE = "token expire";
    // Uid in token doesn't match the declare uid, may be from attacker
    public static String ERROR_TOKEN_NOT_MATCH = "token not match";
    // Token doesn't have the scope to access specific service
    public static String ERROR_TOKEN_NO_SCOPE = "token no scope";
}
