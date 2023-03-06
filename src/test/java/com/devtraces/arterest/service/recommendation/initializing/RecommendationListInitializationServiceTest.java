package com.devtraces.arterest.service.recommendation.initializing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.FollowRecommendation;
import com.devtraces.arterest.model.recommendation.FollowRecommendationRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationListInitializationServiceTest {

    @Mock
    private FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    @Mock
    private FollowRecommendationCacheRepository followRecommendationCacheRepository;
    @Mock
    private FollowRecommendationRepository followRecommendationRepository;
    @Mock
    private LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;
    @Mock
    private FeedRecommendationCacheRepository feedRecommendationCacheRepository;
    @Mock
    private LikeRecommendationRepository likeRecommendationRepository;
    @InjectMocks
    private RecommendationListInitializationService recommendationListInitializationService;

    @Test
    @DisplayName("팔로우 및 게시물 추천을 위한 두 가지 리스트 캐시서버에 초기화 성공")
    void test(){
        // given
        List<Long> followRecommendationList = new ArrayList<>();
        followRecommendationList.add(1L);
        followRecommendationList.add(2L);
        followRecommendationList.add(3L);

        FollowRecommendation followRecommendationEntity = FollowRecommendation.builder()
            .id(1L)
            .followRecommendationTargetUsers(followRecommendationList.toString())
            .build();

        List<Long> feedRecommendationList = new ArrayList<>();
        feedRecommendationList.add(1L);
        feedRecommendationList.add(2L);
        feedRecommendationList.add(3L);

        LikeRecommendation likeRecommendationEntity = LikeRecommendation.builder()
            .id(1L)
            .RecommendationTargetFeeds(feedRecommendationList.toString())
            .build();

        given(followSamplePoolCacheRepository.getSampleList()).willReturn(followRecommendationList);

        doNothing().when(followRecommendationCacheRepository)
            .updateRecommendationTargetUserIdList(anyList());

        given(followRecommendationRepository.save(any()))
            .willReturn(followRecommendationEntity);

        given(likeSamplePoolCacheRepository.getSampleList()).willReturn(feedRecommendationList);

        doNothing().when(feedRecommendationCacheRepository)
            .updateRecommendationTargetFeedIdList(anyList());

        given(likeRecommendationRepository.save(any()))
            .willReturn(likeRecommendationEntity);

        // when
        recommendationListInitializationService
            .initializeFollowAndFeedRecommendationListToCacheServer();

        // then
        verify(followSamplePoolCacheRepository, times(1)).getSampleList();
        verify(followRecommendationCacheRepository, times(1))
            .updateRecommendationTargetUserIdList(anyList());
        verify(followRecommendationRepository, times(1))
            .save(any());
        verify(likeSamplePoolCacheRepository, times(1)).getSampleList();
        verify(feedRecommendationCacheRepository, times(1))
            .updateRecommendationTargetFeedIdList(anyList());
        verify(likeRecommendationRepository, times(1))
            .save(any());
    }
}