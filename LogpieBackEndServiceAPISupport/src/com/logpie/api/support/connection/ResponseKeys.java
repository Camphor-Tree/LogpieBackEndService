package com.logpie.api.support.connection;

public class ResponseKeys
{

    /**
     * Keys for different service response
     */
    public static final String KEY_AUTHENTICATION_RESULT = "com.logpie.authentication.result";
    public static final String KEY_CUSTOMER_RESULT = "com.logpie.customer.result";
    public static final String KEY_ACTIVITY_RESULT = "com.logpie.activity.result";
    public static final String KEY_COMMENT_RESULT = "com.logpie.activity.result";

    /**
     * Keys for token validation in Authentication Service
     */
    public static final String KEY_TOKEN_VALIDATION_FAIL_REASON = "com.logpie.authentication.token.validation.fail.reason";

    /**
     * Keys for common keywords
     */
    public static final String KEY_REQUEST_TYPE = "com.logpie.request.type";
    public static final String KEY_METADATA = "com.logpie.result.metadata";
    public static final String KEY_RESPONSE_ID = "com.logpie.response.id";
    public static final String KEY_ERROR_MESSAGE = "com.logpie.error.message";

    public static final String KEY_SERVER_ERROR_MESSAGE = "com.logpie.server.error.message";
    public static final String KEY_RESULT_ERROR_MESSAGE = "com.logpie.result.error.message";

    public static final String REQUEST_TYPE_INSERT = "insert";
    public static final String REQUEST_TYPE_QUERY = "query";
    public static final String REQUEST_TYPE_UPDATE = "update";
    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_ERROR = "error";

    // Key
    public static final String KEY_RESPONSE_MODE = "com.logpie.response.mode";
    // Value
    public static final String MODE_REFRESH = "refresh";
    public static final String MODE_LOAD_MORE = "load_more";

    /**
     * Keys for all Logpie keywords in Android
     */
    public static final String KEY_UID = "com.logpie.uid";
    public static final String KEY_EMAIL = "com.logpie.email";
    public static final String KEY_ACCESS_TOKEN = "com.logpie.access.token";
    public static final String KEY_ACCESS_TOKEN_EXPIRATION = "com.logpie.access.token.expiration";
    public static final String KEY_REFRESH_TOKEN = "com.logpie.refresh.token";
    public static final String KEY_REFRESH_TOKEN_EXPIRATION = "com.logpie.refresh.token.expiration";

    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_BIRTHDAY = "com.logpie.birthday";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "com.logpie.country";
    public static final String KEY_LAST_UPDATE_TIME = "com.logpie.last.update.time";
    public static final String KEY_IS_ORGANIZATION = "com.logpie.is.organization";

    public static final String KEY_AID = "com.logpie.aid";
    public static final String KEY_DESCRIPTION = "com.logpie.description";
    public static final String KEY_LOCATION = "com.logpie.location";
    public static final String KEY_LATITUDE = "com.logpie.latitude";
    public static final String KEY_LONGITUDE = "com.logpie.longitude";
    public static final String KEY_CREATE_TIME = "com.logpie.create.time";
    public static final String KEY_START_TIME = "com.logpie.start.time";
    public static final String KEY_END_TIME = "com.logpie.end.time";
    public static final String KEY_COMMENT = "com.logpie.comment";
    public static final String KEY_COUNT_LIKE = "com.logpie.count.like";
    public static final String KEY_COUNT_DISLIKE = "com.logpie.count.dislike";
    public static final String KEY_ACTIVATED = "com.logpie.activated";
    public static final String KEY_CATEGORY_ID = "com.logpie.category";
    public static final String KEY_SUBCATEGORY_ID = "com.logpie.subcategory";

    /**
     * Key response to comments
     * */
    public static final String KEY_SENDER_USER_ID = "user_id";
    public static final String KEY_SEND_TO_ACTIVITYID = "activity_id";
    public static final String KEY_COMMENT_CONTENT = "comment_content";
    public static final String KEY_COMMENT_TIME = "comment_time";

}
