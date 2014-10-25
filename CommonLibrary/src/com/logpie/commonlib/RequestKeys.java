package com.logpie.commonlib;

public class RequestKeys
{

    /**
     * REQUIRED REQUEST ELEMENT KEYS AND SQL KEYS
     */

    // Key
    public static final String KEY_REQUEST_SERVICE = "request_service";
    // Value
    public static final String SERVICE_LOGIN = "login";
    public static final String SERVICE_RESET_PASSWORD = "reset_password";
    public static final String SERVICE_FORGET_PASSWORD = "forget_password";
    public static final String SERVICE_TOKEN_EXANGE = "token_exchange";
    public static final String SERVICE_TOKEN_VALIDATION = "token_validation";

    public static final String SERVICE_REGISTER = "register";
    public static final String SERVICE_SHOW_PROFILE = "show_profile";
    public static final String SERVICE_EDIT_PROFILE = "edit_profile";

    public static final String SERVICE_CREATE_ACTIVITY = "create_activity";
    public static final String SERVICE_FIND_NEARBY_ACTIVITY = "find_nearby_activity";
    public static final String SERVICE_FIND_ACTIVITY_BY_CITY = "find_activity_by_city";
    public static final String SERVICE_FIND_ACTIVITY_BY_CATEGORY = "find_activity_by_category";
    public static final String SERVICE_SHOW_ACTIVITY_DETAIL = "show_activity_detail";
    public static final String SERVICE_EDIT_ACTIVITY_DETAIL = "edit_activity_detail";
    public static final String SERVICE_LIKE_ACTIVITY = "like_activity";
    public static final String SERVICE_DISLIKE_ACTIVITY = "dislike_activity";

    // For comment to activity service keys
    public static final String SERVICE_SHOW_COMMENTS = "find_comments";
    public static final String SERVICE_INSERT_COMMENT_TO_ACTIVITY = "create_comment";

    // Key
    public static final String KEY_REQUEST_TYPE = "request_type";
    // Value
    public static final String REQUEST_TYPE_INSERT = "insert";
    public static final String REQUEST_TYPE_QUERY = "query";
    public static final String REQUEST_TYPE_UPDATE = "update";

    // Key for request ID
    // Each httpRequest should have a unique request ID.
    public static final String KEY_REQUEST_ID = "request_id";

    // Key for JSONArray
    public static final String KEY_INSERT_KEYVALUE_PAIR = "insert_keyvalue_pair";
    // Keys in the JSONArray
    public static final String KEY_INSERT_COLUMN = "insert_column";
    public static final String KEY_INSERT_VALUE = "insert_value";

    // Key for JSONArray
    public static final String KEY_QUERY_KEY = "query_key";
    // Keys in the JSONArray
    public static final String KEY_QUERY_COLUMN = "query_column";

    // Key for JSONArray
    public static final String KEY_UPDATE_KEYVALUE_PAIR = "update_keyvalue_pair";
    // Keys in the JSONArray
    public static final String KEY_UPDATE_COLUMN = "update_column";
    public static final String KEY_UPDATE_VALUE = "update_value";

    // Key for JSONArray
    public static final String KEY_CONSTRAINT_KEYVALUE_PAIR = "constraint_keyvalue_pair";
    // Keys in the JSONArray
    public static final String KEY_CONSTRAINT_COLUMN = "constraint_column";
    public static final String KEY_CONSTRAINT_OPERATOR = "constraint_operator";
    public static final String KEY_CONSTRAINT_VALUE = "constraint_value";

    /**
     * OTHER OPTIONAL SQL KEYS
     */
    public static final String KEY_COUNT = "count";
    public static final String KEY_MAX = "max";
    public static final String KEY_MIN = "min";
    public static final String KEY_LIMIT_NUMBER = "limit_number";
    public static final String KEY_ORDER_BY = "order_by";
    public static final String KEY_DESC = "desc";
    public static final String KEY_EQUAL = "=";
    public static final String KEY_LESS_THAN = "<";
    public static final String KEY_MORE_THAN = ">";

    /**
     * Keys for all Logpie keywords
     */
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

    public static final String KEY_AID = "aid";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_CREATE_TIME = "create_time";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_COUNT_LIKE = "count_like";
    public static final String KEY_COUNT_DISLIKE = "count_dislike";
    public static final String KEY_ACTIVATED = "activated";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_SUBCATEGORY = "subcategory";
    public static final String KEY_ACTIVITY_MODE = "mode";

    // For Comment Feature
    public static final String KEY_SENDER_USER_ID = "user_id";
    public static final String KEY_SEND_TO_ACTIVITYID = "activity_id";
    public static final String KEY_COMMENT_CONTENT = "comment_content";
    public static final String KEY_COMMENT_TIME = "comment_time";

    public static final String KEY_CID = "cid";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_PROVINCE = "province";

    /**
     * Keys for token validation
     */
    public static final String KEY_DECLARE_UID = "uid";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_ACCESS_SERVICE = "access_service";

    /**
     * Keys for token exchange
     */
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    // KEY_DECLARE_UID is same as Key for token validation

    /**
     * Keys for reset password
     */
    public static final String KEY_NEW_PASSWORD = "new_password";
    // KEY_DECLARE_UID is same as Key for token validation

    /**
     * Keys for OOD
     */

}
