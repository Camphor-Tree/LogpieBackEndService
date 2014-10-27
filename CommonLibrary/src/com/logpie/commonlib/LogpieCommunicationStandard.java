package com.logpie.commonlib;

public class LogpieCommunicationStandard
{
    private static final String REQUEST_ID = "request_id";
    private static final String REQUEST_SERVICE = "request_service";
    private static final String REQUEST_TYPE = "request_type";

    private static final String INSERT_KEYVALUE = "insert_keyvalue";
    private static final String QUERY_KEY = "query_key";
    private static final String UPDATE_KEYVALUE = "update_keyvalue";
    private static final String CONTRAINTS = "contraints";

    private static final String COLUMN = "column";
    private static final String OPERATOR = "operator";
    private static final String VALUE = "value";

    private static final String TYPE_INSERT = "insert";
    private static final String TYPE_QUERY = "query";
    private static final String TYPE_UPDATE = "update";

    private static final String OPERATOR_EQUAL = "=";
    private static final String OPERATOR_LESS_THAN = "<";
    private static final String OPERATOR_MORE_THAN = ">";

    private static final String RESPONSE_ID = "response_id";
    private static final String RESPONSE_SERVICE = "response_service";
    private static final String RESPONSE_RESULT = "response_result";

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    public class AuthenticationService
    {
        class RequestKeys
        {

        }

        class RequestValues
        {

        }

        class ResponseKeys
        {
            public static final String RESPONSE_ID = LogpieCommunicationStandard.RESPONSE_ID;
            public static final String RESPONSE_SERVICE = LogpieCommunicationStandard.RESPONSE_SERVICE;
            public static final String RESPONSE_RESULT = LogpieCommunicationStandard.RESPONSE_RESULT;

            public static final String SERVER_ERROR_MESSAGE = ErrorMessage.SERVER_ERROR_MESSAGE;
            public static final String SERVICE_ERROR_MESSAGE = ErrorMessage.SERVICE_ERROR_MESSAGE;
        }

        class ResponseValues
        {
            public static final String SUCCESS = LogpieCommunicationStandard.SUCCESS;
            public static final String ERROR = LogpieCommunicationStandard.ERROR;

            public static final String SERVER_ERROR_BAD_REQUEST = ErrorMessage.SERVER_ERROR_BAD_REQUEST;
            public static final String SERVER_ERROR_INTERNAL_ERROR = ErrorMessage.SERVER_ERROR_INTERNAL_ERROR;
            public static final String SERVER_ERROR_AUTH_ERROR = ErrorMessage.SERVER_ERROR_AUTH_ERROR;
            public static final String SERVER_ERROR_TOKEN_EXPIRE = ErrorMessage.SERVER_ERROR_TOKEN_EXPIRE;

            public static final String SERVICE_ERROR_SERVICE_DOES_NOT_EXIST = ErrorMessage.SERVICE_ERROR_SERVICE_DOES_NOT_EXIST;
            public static final String SERVICE_ERROR_MISSING_REQUIRED_DATA = ErrorMessage.SERVICE_ERROR_MISSING_REQUIRED_DATA;
            public static final String SERVICE_ERROR_JSON_FORMAT_ERROR = ErrorMessage.SERVICE_ERROR_JSON_FORMAT_ERROR;
            public static final String SERVICE_ERROR_EMAIL_ALREADY_EXISTS = ErrorMessage.SERVICE_ERROR_EMAIL_ALREADY_EXISTS;
            public static final String SERVICE_ERROR_PASSWORD_DOES_NOT_MATCH = ErrorMessage.SERVICE_ERROR_PASSWORD_DOES_NOT_MATCH;
        }
    }

    public class CustomerService
    {
        class RequestKeys
        {
            public static final String REQUEST_ID = LogpieCommunicationStandard.REQUEST_ID;
            public static final String REQUEST_SERVICE = LogpieCommunicationStandard.REQUEST_SERVICE;
            public static final String REQUEST_TYPE = LogpieCommunicationStandard.REQUEST_TYPE;

            public static final String INSERT_KEYVALUE = LogpieCommunicationStandard.INSERT_KEYVALUE;
            public static final String QUERY_KEY = LogpieCommunicationStandard.QUERY_KEY;
            public static final String UPDATE_KEYVALUE = LogpieCommunicationStandard.UPDATE_KEYVALUE;
            public static final String CONTRAINTS = LogpieCommunicationStandard.CONTRAINTS;

            public static final String COLUMN = LogpieCommunicationStandard.COLUMN;
            public static final String OPERATOR = LogpieCommunicationStandard.OPERATOR;
            public static final String VALUE = LogpieCommunicationStandard.VALUE;

        }

        class RequestValues
        {
            public static final String TYPE_INSERT = LogpieCommunicationStandard.TYPE_INSERT;
            public static final String TYPE_QUERY = LogpieCommunicationStandard.TYPE_QUERY;
            public static final String TYPE_UPDATE = LogpieCommunicationStandard.TYPE_UPDATE;

            public static final String SERVICE_REGISTER = "register";
            public static final String SERVICE_SHOW_PROFILE = "show_profile";
            public static final String SERVICE_EDIT_PROFILE = "edit_profile";

            public static final String OPERATOR_EQUAL = LogpieCommunicationStandard.OPERATOR_EQUAL;
            public static final String OPERATOR_LESS_THAN = LogpieCommunicationStandard.OPERATOR_LESS_THAN;
            public static final String OPERATOR_MORE_THAN = LogpieCommunicationStandard.OPERATOR_MORE_THAN;

            public static final String USER_UID = DatabaseSchema.USER_UID;
            public static final String USER_EMAIL = DatabaseSchema.USER_EMAIL;
            public static final String USER_NICKNAME = DatabaseSchema.USER_NICKNAME;
            public static final String USER_GENDER = DatabaseSchema.USER_GENDER;
            public static final String USER_BIRTHDAY = DatabaseSchema.USER_BIRTHDAY;
            public static final String USER_CITY = DatabaseSchema.USER_CITY;
            public static final String USER_COUNTRY = DatabaseSchema.USER_COUNTRY;
            public static final String USER_LAST_UPDATE_TIME = DatabaseSchema.USER_LAST_UPDATE_TIME;
            public static final String USER_IS_ORGANIZATION = DatabaseSchema.USER_IS_ORGANIZATION;
        }

        class ResponseKeys
        {
            public static final String RESPONSE_ID = LogpieCommunicationStandard.RESPONSE_ID;
            public static final String RESPONSE_SERVICE = LogpieCommunicationStandard.RESPONSE_SERVICE;
            public static final String RESPONSE_RESULT = LogpieCommunicationStandard.RESPONSE_RESULT;

            public static final String SERVER_ERROR_MESSAGE = ErrorMessage.SERVER_ERROR_MESSAGE;
            public static final String SERVICE_ERROR_MESSAGE = ErrorMessage.SERVICE_ERROR_MESSAGE;
        }

        class ResponseValues
        {
            public static final String SUCCESS = LogpieCommunicationStandard.SUCCESS;
            public static final String ERROR = LogpieCommunicationStandard.ERROR;

            public static final String SERVER_ERROR_BAD_REQUEST = ErrorMessage.SERVER_ERROR_BAD_REQUEST;
            public static final String SERVER_ERROR_INTERNAL_ERROR = ErrorMessage.SERVER_ERROR_INTERNAL_ERROR;
            public static final String SERVER_ERROR_AUTH_ERROR = ErrorMessage.SERVER_ERROR_AUTH_ERROR;
            public static final String SERVER_ERROR_TOKEN_EXPIRE = ErrorMessage.SERVER_ERROR_TOKEN_EXPIRE;

            public static final String SERVICE_ERROR_SERVICE_DOES_NOT_EXIST = ErrorMessage.SERVICE_ERROR_SERVICE_DOES_NOT_EXIST;
            public static final String SERVICE_ERROR_MISSING_REQUIRED_DATA = ErrorMessage.SERVICE_ERROR_MISSING_REQUIRED_DATA;
            public static final String SERVICE_ERROR_JSON_FORMAT_ERROR = ErrorMessage.SERVICE_ERROR_JSON_FORMAT_ERROR;
        }
    }

    public class ActivityService
    {
        class RequestKeys
        {
            public static final String REQUEST_ID = LogpieCommunicationStandard.REQUEST_ID;
            public static final String REQUEST_SERVICE = LogpieCommunicationStandard.REQUEST_SERVICE;

            public static final String INSERT_KEYVALUE = LogpieCommunicationStandard.INSERT_KEYVALUE;
            public static final String QUERY_KEY = LogpieCommunicationStandard.QUERY_KEY;
            public static final String UPDATE_KEYVALUE = LogpieCommunicationStandard.UPDATE_KEYVALUE;
            public static final String CONTRAINTS = LogpieCommunicationStandard.CONTRAINTS;

            public static final String COLUMN = LogpieCommunicationStandard.COLUMN;
            public static final String OPERATOR = LogpieCommunicationStandard.OPERATOR;
            public static final String VALUE = LogpieCommunicationStandard.VALUE;

        }

        class RequestValues
        {
            public static final String TYPE_INSERT = LogpieCommunicationStandard.TYPE_INSERT;
            public static final String TYPE_QUERY = LogpieCommunicationStandard.TYPE_QUERY;
            public static final String TYPE_UPDATE = LogpieCommunicationStandard.TYPE_UPDATE;

            public static final String SERVICE_CREATE_ACTIVITY = "create_activity";
            public static final String SERVICE_FIND_NEARBY_ACTIVITY = "find_nearby_activity";
            public static final String SERVICE_FIND_ACTIVITY_BY_CITY = "find_activity_by_city";
            public static final String SERVICE_FIND_ACTIVITY_BY_CATEGORY = "find_activity_by_category";
            public static final String SERVICE_SHOW_ACTIVITY_DETAIL = "show_activity_detail";
            public static final String SERVICE_EDIT_ACTIVITY_DETAIL = "edit_activity_detail";
            public static final String SERVICE_LIKE_ACTIVITY = "like_activity";
            public static final String SERVICE_DISLIKE_ACTIVITY = "dislike_activity";

            public static final String OPERATOR_EQUAL = LogpieCommunicationStandard.OPERATOR_EQUAL;
            public static final String OPERATOR_LESS_THAN = LogpieCommunicationStandard.OPERATOR_LESS_THAN;
            public static final String OPERATOR_MORE_THAN = LogpieCommunicationStandard.OPERATOR_MORE_THAN;

            public static final String ACTIVITY_LATITUDE = "latitude";
            public static final String ACTIVITY_LONGITUDE = "longitude";
            public static final String ACTIVITY_AID = DatabaseSchema.ACTIVITY_AID;
            public static final String ACTIVITY_DESCRIPTION = DatabaseSchema.ACTIVITY_DESCRIPTION;
            public static final String ACTIVITY_LOCATION = DatabaseSchema.ACTIVITY_LOCATION;
            public static final String ACTIVITY_CREATE_TIME = DatabaseSchema.ACTIVITY_CREATE_TIME;
            public static final String ACTIVITY_START_TIME = DatabaseSchema.ACTIVITY_START_TIME;
            public static final String ACTIVITY_END_TIME = DatabaseSchema.ACTIVITY_END_TIME;
            public static final String ACTIVITY_COMMENT = DatabaseSchema.ACTIVITY_COMMENT;
            public static final String ACTIVITY_COUNT_LIKE = DatabaseSchema.ACTIVITY_COUNT_LIKE;
            public static final String ACTIVITY_COUNT_DISLIKE = DatabaseSchema.ACTIVITY_COUNT_DISLIKE;
            public static final String ACTIVITY_CATEGORY = DatabaseSchema.ACTIVITY_CATEGORY;
            public static final String ACTIVITY_SUBCATEGORY = DatabaseSchema.ACTIVITY_SUBCATEGORY;

        }

        class ResponseKeys
        {
            public static final String RESPONSE_ID = LogpieCommunicationStandard.RESPONSE_ID;
            public static final String RESPONSE_SERVICE = LogpieCommunicationStandard.RESPONSE_SERVICE;
            public static final String RESPONSE_RESULT = LogpieCommunicationStandard.RESPONSE_RESULT;

            public static final String SERVER_ERROR_MESSAGE = ErrorMessage.SERVER_ERROR_MESSAGE;
            public static final String SERVICE_ERROR_MESSAGE = ErrorMessage.SERVICE_ERROR_MESSAGE;
        }

        class ResponseValues
        {
            public static final String SUCCESS = LogpieCommunicationStandard.SUCCESS;
            public static final String ERROR = LogpieCommunicationStandard.ERROR;

            public static final String SERVER_ERROR_BAD_REQUEST = ErrorMessage.SERVER_ERROR_BAD_REQUEST;
            public static final String SERVER_ERROR_INTERNAL_ERROR = ErrorMessage.SERVER_ERROR_INTERNAL_ERROR;
            public static final String SERVER_ERROR_AUTH_ERROR = ErrorMessage.SERVER_ERROR_AUTH_ERROR;
            public static final String SERVER_ERROR_TOKEN_EXPIRE = ErrorMessage.SERVER_ERROR_TOKEN_EXPIRE;

            public static final String SERVICE_ERROR_SERVICE_DOES_NOT_EXIST = ErrorMessage.SERVICE_ERROR_SERVICE_DOES_NOT_EXIST;
            public static final String SERVICE_ERROR_MISSING_REQUIRED_DATA = ErrorMessage.SERVICE_ERROR_MISSING_REQUIRED_DATA;
            public static final String SERVICE_ERROR_JSON_FORMAT_ERROR = ErrorMessage.SERVICE_ERROR_JSON_FORMAT_ERROR;
        }
    }

    public class CommentService
    {
        class RequestKeys
        {
            public static final String REQUEST_ID = LogpieCommunicationStandard.REQUEST_ID;
            public static final String REQUEST_SERVICE = LogpieCommunicationStandard.REQUEST_SERVICE;
            public static final String REQUEST_TYPE = LogpieCommunicationStandard.REQUEST_TYPE;

            public static final String INSERT_KEYVALUE = LogpieCommunicationStandard.INSERT_KEYVALUE;
            public static final String QUERY_KEY = LogpieCommunicationStandard.QUERY_KEY;
            public static final String CONTRAINTS = LogpieCommunicationStandard.CONTRAINTS;

            public static final String COLUMN = LogpieCommunicationStandard.COLUMN;
            public static final String OPERATOR = LogpieCommunicationStandard.OPERATOR;
            public static final String VALUE = LogpieCommunicationStandard.VALUE;
        }

        class RequestValues
        {
            public static final String TYPE_INSERT = LogpieCommunicationStandard.TYPE_INSERT;
            public static final String TYPE_QUERY = LogpieCommunicationStandard.TYPE_QUERY;

            public static final String SERVICE_SHOW_COMMENTS = "find_comments";
            public static final String SERVICE_CREATE_COMMENT = "create_comment";

            public static final String OPERATOR_EQUAL = LogpieCommunicationStandard.OPERATOR_EQUAL;
            public static final String OPERATOR_LESS_THAN = LogpieCommunicationStandard.OPERATOR_LESS_THAN;
            public static final String OPERATOR_MORE_THAN = LogpieCommunicationStandard.OPERATOR_MORE_THAN;

            public static final String COMMENT_SENDER_ID = DatabaseSchema.COMMENT_SENDER_ID;
            public static final String COMMENT_ACTIVITY_ID = DatabaseSchema.COMMENT_ACTIVITY_ID;
            public static final String COMMENT_CONTENT = DatabaseSchema.COMMENT_CONTENT;
            public static final String COMMENT_CREATE_TIME = DatabaseSchema.COMMENT_CREATE_TIME;
        }

        class ResponseKeys
        {
            public static final String RESPONSE_ID = LogpieCommunicationStandard.RESPONSE_ID;
            public static final String RESPONSE_SERVICE = LogpieCommunicationStandard.RESPONSE_SERVICE;
            public static final String RESPONSE_RESULT = LogpieCommunicationStandard.RESPONSE_RESULT;

            public static final String SERVER_ERROR_MESSAGE = ErrorMessage.SERVER_ERROR_MESSAGE;
            public static final String SERVICE_ERROR_MESSAGE = ErrorMessage.SERVICE_ERROR_MESSAGE;
        }

        class ResponseValues
        {
            public static final String SUCCESS = LogpieCommunicationStandard.SUCCESS;
            public static final String ERROR = LogpieCommunicationStandard.ERROR;

            public static final String SERVER_ERROR_BAD_REQUEST = ErrorMessage.SERVER_ERROR_BAD_REQUEST;
            public static final String SERVER_ERROR_INTERNAL_ERROR = ErrorMessage.SERVER_ERROR_INTERNAL_ERROR;
            public static final String SERVER_ERROR_AUTH_ERROR = ErrorMessage.SERVER_ERROR_AUTH_ERROR;
            public static final String SERVER_ERROR_TOKEN_EXPIRE = ErrorMessage.SERVER_ERROR_TOKEN_EXPIRE;

            public static final String SERVICE_ERROR_SERVICE_DOES_NOT_EXIST = ErrorMessage.SERVICE_ERROR_SERVICE_DOES_NOT_EXIST;
            public static final String SERVICE_ERROR_MISSING_REQUIRED_DATA = ErrorMessage.SERVICE_ERROR_MISSING_REQUIRED_DATA;
            public static final String SERVICE_ERROR_JSON_FORMAT_ERROR = ErrorMessage.SERVICE_ERROR_JSON_FORMAT_ERROR;
        }
    }

    public class RocketService
    {

    }

    private class DatabaseSchema
    {
        protected static final String USER_UID = "uid";
        protected static final String USER_EMAIL = "email";
        protected static final String USER_NICKNAME = "nickname";
        protected static final String USER_GENDER = "gender";
        protected static final String USER_BIRTHDAY = "birthday";
        protected static final String USER_CITY = "city";
        protected static final String USER_COUNTRY = "country";
        protected static final String USER_LAST_UPDATE_TIME = "last_update_time";
        protected static final String USER_IS_ORGANIZATION = "is_organization";

        protected static final String ACTIVITY_AID = "aid";
        protected static final String ACTIVITY_DESCRIPTION = "description";
        protected static final String ACTIVITY_LOCATION = "location";
        protected static final String ACTIVITY_CREATE_TIME = "create_time";
        protected static final String ACTIVITY_START_TIME = "start_time";
        protected static final String ACTIVITY_END_TIME = "end_time";
        protected static final String ACTIVITY_COMMENT = "comment";
        protected static final String ACTIVITY_COUNT_LIKE = "count_like";
        protected static final String ACTIVITY_COUNT_DISLIKE = "count_dislike";
        protected static final String ACTIVITY_CATEGORY = "category";
        protected static final String ACTIVITY_SUBCATEGORY = "subcategory";

        protected static final String COMMENT_SENDER_ID = "user_id";
        protected static final String COMMENT_ACTIVITY_ID = "activity_id";
        protected static final String COMMENT_CONTENT = "comment_content";
        protected static final String COMMENT_CREATE_TIME = "comment_time";

    }

    private class ErrorMessage
    {
        protected static final String SERVER_ERROR_MESSAGE = "server_error_message";
        protected static final String SERVICE_ERROR_MESSAGE = "service_error_message";

        protected static final String SERVER_ERROR_BAD_REQUEST = "bad_request";
        protected static final String SERVER_ERROR_INTERNAL_ERROR = "internal_error";
        protected static final String SERVER_ERROR_AUTH_ERROR = "cannot_authenticate_the_user";
        protected static final String SERVER_ERROR_TOKEN_EXPIRE = "token_get_expired";

        // TODO: add more service errors
        protected static final String SERVICE_ERROR_SERVICE_DOES_NOT_EXIST = "service_does_not_exist";
        protected static final String SERVICE_ERROR_MISSING_REQUIRED_DATA = "missing_required_data";
        protected static final String SERVICE_ERROR_JSON_FORMAT_ERROR = "json_format_error";
        protected static final String SERVICE_ERROR_EMAIL_ALREADY_EXISTS = "email_already_exists";
        protected static final String SERVICE_ERROR_PASSWORD_DOES_NOT_MATCH = "password_does_not_match";

    }

}
