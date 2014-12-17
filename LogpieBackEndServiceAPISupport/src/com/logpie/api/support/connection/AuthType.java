package com.logpie.api.support.connection;

public enum AuthType
{
    // When user already logged in
    NormalAuth,
    // When access token is expired, need refresh_token to refresh the
    // access_token
    TokenExchange,
    // No user logged in.
    NoAuth;
}