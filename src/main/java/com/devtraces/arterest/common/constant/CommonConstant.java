package com.devtraces.arterest.common.constant;

public class CommonConstant {

    public static final int CONTENT_LENGTH_LIMIT = 1000;
    public static final int FOLLOW_COUNT_LIMIT = 5000;
    public static final int HASHTAG_COUNT_LIMIT = 10;
    public static final int IMAGE_FILE_COUNT_LIMIT = 15;

    public static final String REDIS_LIKE_NUMBER_KEY_PREFIX = "LikeNumber:";

    public static final String REDIS_FOLLOW_SAMPLE_POOL_LIST_KEY = "FollowSamplePoolList";
    public static final long REDIS_FOLLOW_SAMPLE_POOL_LIST_SIZE = 600L;

    public static final String REDIS_FOLLOW_RECOMMENDATION_LIST_KEY = "FollowRecommendationList";
    public static final int FOLLOW_RECOMMENDATION_LIST_SIZE = 100;

    // 매 6 초마다.
    public static final String PUSH_SAMPLE_TO_REDIS_CRON_STRING = "0/6 * * * * ?";

    // 매 정각마다.
    public static final String INITIALIZE_RECOMMENDATION_LIST_TO_REDIS_CONE_STRING = "0 0 0/1 * * *";
}
