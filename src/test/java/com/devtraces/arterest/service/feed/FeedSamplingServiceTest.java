package com.devtraces.arterest.service.feed;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import com.devtraces.arterest.service.recommendation.FeedSamplingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedSamplingServiceTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;
    @Mock
    private FeedRecommendationCacheRepository feedRecommendationCacheRepository;
    @Mock
    private LikeRecommendationRepository likeRecommendationRepository;
    @InjectMocks
    private FeedSamplingService feedSamplingService;

    @Test
    @DisplayName("좋아요 정보 샘플 캐시서버에 등록 성공")
    void successPushLikeSampleToCacheServer(){
        // given
        Likes likeSampleEntity = Likes.builder()
            .id(1L)
            .userId(1L)
            .feedId(2L)
            .build();

        given(likeRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(likeSampleEntity));
        doNothing().when(likeSamplePoolCacheRepository).pushSample(2L);

        // when
        feedSamplingService.pushLikeSampleToCacheServer();

        // then
        verify(likeRepository, times(1)).findTopByOrderByIdDesc();
        verify(likeSamplePoolCacheRepository, times(1)).pushSample(2L);
    }

    @Test
    @DisplayName("좋아요 개수 기반 추천 탸깃 게시물 리스트 캐시서버에 초기화 성공")
    void InitializeRecommendationTargetFeedIdListToCacheServer(){
        // given
        List<Long> recommendationList = new ArrayList<>();
        recommendationList.add(1L);
        recommendationList.add(2L);
        recommendationList.add(3L);

        doNothing().when(feedRecommendationCacheRepository)
            .updateRecommendationTargetFeedIdList(anyList());

        LikeRecommendation likeRecommendationEntity = LikeRecommendation.builder()
            .id(1L)
            .RecommendationTargetFeeds(recommendationList.toString())
            .build();

        given(likeRecommendationRepository.save(any()))
            .willReturn(likeRecommendationEntity);

        // when
        feedSamplingService.initializeRecommendationTargetFeedIdListToCacheServer();

        // then
        verify(feedRecommendationCacheRepository, times(1))
            .updateRecommendationTargetFeedIdList(anyList());
        verify(likeRecommendationRepository, times(1))
            .save(any());
    }

}