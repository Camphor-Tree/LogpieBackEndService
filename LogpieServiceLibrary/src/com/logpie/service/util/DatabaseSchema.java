package com.logpie.service.util;

public class DatabaseSchema
{
    public static final String SCHEMA_TABLE_USER = "user";
    public static final String SCHEMA_USER_UID = "uid";
    public static final String SCHEMA_USER_PASSWORD = "password";
    public static final String SCHEMA_USER_EMAIL = "email";
    public static final String SCHEMA_USER_NICKNAME = "nickname";
    public static final String SCHEMA_USER_GENDER = "gender";
    public static final String SCHEMA_USER_BIRTHDAY = "birthday";
    public static final String SCHEMA_USER_CITY = "city";
    public static final String SCHEMA_USER_COUNTRY = "country";
    public static final String SCHEMA_USER_LAST_UPDATE_TIME = "last_update_time";
    public static final String SCHEMA_USER_IS_ORGANIZATION = "is_organization";

    public static final String SCHEMA_TABLE_ACTIVITY = "activity";
    public static final String SCHEMA_ACTIVITY_AID = "aid";
    public static final String SCHEMA_ACTIVITY_UID = "uid";
    public static final String SCHEMA_ACTIVITY_NICKNAME = "nickname";
    public static final String SCHEMA_ACTIVITY_DESCRIPTION = "description";
    public static final String SCHEMA_ACTIVITY_CITY = "city";
    public static final String SCHEMA_ACTIVITY_LOCATION = "location";
    public static final String SCHEMA_ACTIVITY_LATLON = "latlon";
    public static final String SCHEMA_ACTIVITY_CREATE_TIME = "create_time";
    public static final String SCHEMA_ACTIVITY_START_TIME = "start_time";
    public static final String SCHEMA_ACTIVITY_END_TIME = "end_time";
    public static final String SCHEMA_ACTIVITY_COMMENT = "comment";
    public static final String SCHEMA_ACTIVITY_COUNT_LIKE = "count_like";
    public static final String SCHEMA_ACTIVITY_COUNT_DISLIKE = "count_dislike";
    public static final String SCHEMA_ACTIVITY_ACTIVATED = "activated";
    public static final String SCHEMA_ACTIVITY_CATEGORY = "category";
    public static final String SCHEMA_ACTIVITY_SUBCATEGORY = "subcategory";

    public static final String SCHEMA_TABLE_COMMENT = "comment";
    public static final String SCHEMA_COMMENTS_COMMENT_ID = "comment_id";
    public static final String SCHEMA_COMMENTS_USER_ID = "user_id";
    public static final String SCHEMA_COMMENTS_ACTIVITY_ID = "activity_id";
    public static final String SCHEMA_COMMENTS_COMMENT_CONTENT = "comment_content";
    public static final String SCHEMA_COMMENTS_COMMENT_TIME = "comment_time";
    // TODO: Implement features for message notification
    public static final String SCHEMA_COMMENTS_REPLY_TO = "reply_to";
    public static final String SCHEMA_COMMENTS_READ_BY_REPLY = "read_by_reply";
    public static final String SCHEMA_COMMENTS_READ_BY_HOST = "read_by_host";

    public static final String SCHEMA_TABLE_USER_LIKE_ACTIVITY = "user_like_activity";

    public static final String SCHEMA_TABLE_USER_DISLIKE_ACTIVITY = "user_dislike_activity";

    public static final String SCHEMA_TABLE_CITY = "city";
    public static final String SCHEMA_CITY_CID = "cid";
    public static final String SCHEMA_CITY_CITY = "city";
    public static final String SCHEMA_CITY_GRADE = "grade";
    public static final String SCHEMA_CITY_PROVINCE = "province";

    public static final String SCHEMA_TABLE_CATEGORY = "act_cate_parent";
    public static final String SCHEMA_CATEGORY_CID = "acpid";
    public static final String SCHEMA_CATEGORY_CATEGORYCN = "category_cn";
    public static final String SCHEMA_CATEGORY_CATEGORYUS = "category_us";
    public static final String SCHEMA_TABLE_SUBCATEGORY = "act_cate_child";
    public static final String SCHEMA_SUBCATEGORY_CID = "accid";
    public static final String SCHEMA_SUBCATEGORY_CATEGORYCN = "category_cn";
    public static final String SCHEMA_SUBCATEGORY_CATEGORYUS = "category_us";
    public static final String SCHEMA_SUBCATEGORY_PARENT = "parent";
}
