package com.logpie.authentication.api;

/**
 * AuthenticationFailReasonEnum represents all the possible fail reasons for
 * verifyToken()
 * 
 * @author yilei
 * 
 */
public enum AuthenticationFailReasonEnum
{
    TokenExpired, TokenFake, TokenUnderScope, TokenUidNotMatch;
}
