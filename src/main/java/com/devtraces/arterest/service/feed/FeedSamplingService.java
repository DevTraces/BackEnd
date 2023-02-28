package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedSamplingService {

    private final LikeRepository likeRepository;
    private final LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;
    private final FeedRecommendationCacheRepository feedRecommendationCacheRepository;
    private final LikeRecommendationRepository likeRecommendationRepository;

    // 매 6초마다 가장 최신 좋아요 정보 샘플을 리스트 형태로 캐시해 둔다.
    @Scheduled(cron = CommonConstant.PUSH_SAMPLE_TO_REDIS_CRON_STRING)
    public void pushLikeSampleToCacheServer(){
        Optional<Likes> optionalLatestLikeInfo = likeRepository.findTopByOrderByIdDesc();
        optionalLatestLikeInfo.ifPresent(
            likes -> likeSamplePoolCacheRepository.pushSample(likes.getFeedId())
        );
    }

    // 매 정각마다 최근 1시간 아내에 가장 좋아요를 많이 받은 게시물 100개(혹은 그 이하)를
    // 레디스에 리스트 형태로 초기화 해 둔다.
    @Scheduled(cron = CommonConstant.INITIALIZE_RECOMMENDATION_LIST_TO_REDIS_CRON_STRING)
    public void initializeRecommendationTargetFeedIdListToCacheServer(){
        List<Long> sampleList = likeSamplePoolCacheRepository.getSampleList();
        if(sampleList != null){
            // [ 주키 아이디 : 좋아요 샘플 횟수 카운트 ] 맵 구성.
            Map<Long, Integer> feedIdToCountMap = sampleList.stream().collect(
                Collectors.toMap(Function.identity(), e -> 1, Math::addExact)
            );

            // 맵 내용물 중에서 카운트 높은 횟수 100개(100개 미만일 경우 중간 브레이크) 골라내기.
            PriorityQueue<Entry<Long, Integer>> priorityQueue = new PriorityQueue<>(
                (x,y) -> (y.getValue() - x.getValue())
            );
            List<Long> recommendationList = new ArrayList<>();
            for(int i=1; i<= CommonConstant.FEED_RECOMMENDATION_LIST_SIZE; i++){
                if(!priorityQueue.isEmpty()){
                    recommendationList.add(priorityQueue.poll().getKey());
                } else break;
            }

            feedRecommendationCacheRepository
                .updateRecommendationTargetFeedIdList(recommendationList);

            // 캐시서버가 다운됐을 때를 대비하여 DB에도 저장함.
            StringBuilder builder = new StringBuilder();
            for(Long feedId : recommendationList){
                builder.append(feedId);
                builder.append(",");
            }
            likeRecommendationRepository.save(
                LikeRecommendation.builder()
                    .RecommendationTargetFeeds(builder.toString())
                    .build()
            );
        }
    }

}
