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
    
}
