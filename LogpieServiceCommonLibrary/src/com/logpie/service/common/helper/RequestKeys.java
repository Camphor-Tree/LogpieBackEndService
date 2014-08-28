package com.logpie.service.common.helper;

public class RequestKeys {
	
	/**
	 * Keys for different service types
	 */
	public static final String KEY_AUTHENTICATION_TYPE = "authentication_type";
	public static final String KEY_CUSTOMER_TYPE = "customer_type";
	public static final String KEY_ACTIVITY_TYPE = "activity_type";
	
	
	/**
	 * Key for request ID
	 * 
	 * Each httpRequest should have a unique request ID.
	 */
    public static final String KEY_REQUEST_ID = "request_id";
    
    
    /**
     * Keys for all Logpie keywords in database
     */
    public static final String KEY_TABLE_USER = "user";
    public static final String KEY_UID = "uid";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_LAST_UPDATE_TIME = "last_update_time";
    public static final String KEY_IS_ORGANIZATION = "is_organization";
    
    public static final String KEY_TABLE_ACTIVITY = "activity";
    public static final String KEY_AID = "aid";
    public static final String KEY_CREATE_USER = "create_user";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CREATE_TIME = "create_time";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_COUNT_LIKE = "count_like";
    public static final String KEY_COUNT_DISLIKE = "count_dislike";
    public static final String KEY_ACTIVATED = "activated";
    public static final String KEY_CATEGORY = "category";
    
    public static final String KEY_TABLE_COMMENTS = "comments";
    public static final String KEY_SENDER_USER_ID = "sender_user_id";
    public static final String KEY_SEND_TO_USER_ID = "sendto_user_id";
    public static final String KEY_SENDER_USER_NAME = "sender_username";
    public static final String KEY_SEND_TO_USER_NAME = "sendto_username";
    public static final String KEY_COMMENT_CONTENT = "comment_content";
    public static final String KEY_COMMENT_TIME = "comment_time";
    public static final String KEY_READ_BY_REPLY = "read_by_reply";
    public static final String KEY_READ_BY_HOST = "read_by_host";
    
    public static final String KEY_TABLE_USER_LIKE_ACTIVITY = "user_like_activity";
    
    public static final String KEY_TABLE_USER_DISLIKE_ACTIVITY = "user_dislike_activity";
    
    public static final String KEY_TABLE_CITY = "city";
    public static final String KEY_CID = "cid";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_PROVINCE = "province";
    
    
    /**
     * Keys for OOD
     */
    
    
    
    /**
     * Keys for SQL syntax
     */
    public static final String KEY_INSERT = "insert";
    public static final String KEY_UPDATE = "update";
    public static final String KEY_QUERY = "query";
    public static final String KEY_KEYWORD = "key";
    public static final String KEY_VALUE = "value";
    public static final String KEY_CONSTRAINT_KEYWORD = "constraint_key";
    public static final String KEY_CONSTRAINT_VALUE = "constraint_value";
}
