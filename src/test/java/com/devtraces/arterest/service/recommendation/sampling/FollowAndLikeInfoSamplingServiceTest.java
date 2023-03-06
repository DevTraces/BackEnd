package com.devtraces.arterest.service.recommendation.sampling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowAndLikeInfoSamplingServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;
    @InjectMocks
    private FollowAndLikeInfoSamplingService followAndLikeInfoSamplingService;

    @Test
    @DisplayName("좋아요 정보와 팔로우 정보 샘플 캐시서버에 등록 성공")
    void successPushFollowAndLikeSampleToCacheServer(){
        // given
        Follow follow = Follow.builder()
            .id(1L)
            .followingId(2L)
            .build();

        Likes likeSampleEntity = Likes.builder()
            .id(1L)
            .userId(1L)
            .feedId(2L)
            .build();

        given(followRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(follow));
        doNothing().when(followSamplePoolCacheRepository).pushSample(2L);

        given(likeRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(likeSampleEntity));
        doNothing().when(likeSamplePoolCacheRepository).pushSample(2L);

        // when
        followAndLikeInfoSamplingService.pushFollowAndLikeSampleToCacheServer();

        // then
        verify(followRepository, times(1)).findTopByOrderByIdDesc();
        verify(followSamplePoolCacheRepository, times(1)).pushSample(2L);
        verify(likeRepository, times(1)).findTopByOrderByIdDesc();
        verify(likeSamplePoolCacheRepository, times(1)).pushSample(2L);
    }

}