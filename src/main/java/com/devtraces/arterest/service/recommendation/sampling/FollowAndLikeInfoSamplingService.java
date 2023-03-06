package com.devtraces.arterest.service.recommendation.sampling;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowAndLikeInfoSamplingService {

    private final FollowRepository followRepository;
    private final FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    private final LikeRepository likeRepository;
    private final LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;

    // 매 6초마다 가장 최신 팔로우 정보 샘플을 리스트 형태로 캐시해 둔다.
    // 매 6초마다 가장 최신 좋아요 정보 샘플을 리스트 형태로 캐시해 둔다.
    @Scheduled(cron = CommonConstant.PUSH_SAMPLE_TO_REDIS_CRON_STRING)
    public void pushLikeSampleToCacheServer(){
        Optional<Follow> optionalLatestFollow = followRepository.findTopByOrderByIdDesc();
        optionalLatestFollow
            .ifPresent(
                follow -> followSamplePoolCacheRepository.pushSample(follow.getFollowingId())
            );

        Optional<Likes> optionalLatestLikeInfo = likeRepository.findTopByOrderByIdDesc();
        optionalLatestLikeInfo.ifPresent(
            likes -> likeSamplePoolCacheRepository.pushSample(likes.getFeedId())
        );
    }

}
