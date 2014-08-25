package com.logpie.auth.logic;

public class AuthRequestKeys
{
    public static final String KEY_AUTHENTICATION_TYPE = "auth_type";
    public static final String KEY_REQUEST_ID = "request_id";
    // For register
    public static final String KEY_REGISTER_EMAIL = "register_email";
    public static final String KEY_REGISTER_PASSWORD = "register_password";
    public static final String KEY_REGISTER_NICKNAME = "register_nickname";
    public static final String KEY_REGISTER_CITY = "register_city";
    // For login/authenticate
    public static final String KEY_LOGIN_EMAIL = "login_email";
    public static final String KEY_LOGIN_PASSWORD = "login_password";
    // For reset password
    public static final String KEY_CHANGE_PASSWORD_EMAIL = "change_password_email";
    public static final String KEY_CHANGE_PASSWORD_NEW_PASSWORD = "change_password_new_password";
    public static final String KEY_CHANGE_PASSWORD_OLD_PASSWORD = "change_password_old_password";
    
}
