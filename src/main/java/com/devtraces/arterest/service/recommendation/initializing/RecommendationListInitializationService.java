package com.devtraces.arterest.service.recommendation.initializing;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.FollowRecommendation;
import com.devtraces.arterest.model.recommendation.FollowRecommendationRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationListInitializationService {

    private final FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    private final FollowRecommendationCacheRepository followRecommendationCacheRepository;
    private final FollowRecommendationRepository followRecommendationRepository;
    private final LikeSamplePoolCacheRepository likeSamplePoolCacheRepository;
    private final FeedRecommendationCacheRepository feedRecommendationCacheRepository;
    private final LikeRecommendationRepository likeRecommendationRepository;

    // 매 정각마다 최근 1시간 이내에 팔로우를 많이 받은 상위 일정 수 만큼의 유저들의 주키 아이디 값 리스트를 캐시해 둔다.
    // 매 정각마다 최근 1시간 아내에 가장 좋아요를 많이 받은 게시물 100개(혹은 그 이하)를
    // 레디스에 리스트 형태로 초기화 해 둔다.
    @Scheduled(cron = CommonConstant.INITIALIZE_RECOMMENDATION_LIST_TO_REDIS_CRON_STRING)
    public void initializeFollowAndFeedRecommendationListToCacheServer(){
        List<Long> followSampleList = followSamplePoolCacheRepository.getSampleList();
        if(followSampleList != null){
            // 주키 아이디 : 팔로우 받은 횟수 카운트 맵 구성.
            Map<Long, Integer> userIdToCountMap = followSampleList.stream().collect(
                Collectors.toMap(Function.identity(), e -> 1, Math::addExact)
            );

            // 맵 내용물 중에서 카운트 높은 횟수 100개 (100개 보다 적다면 중간에 브레이크) 골라내기.
            PriorityQueue<Map.Entry<Long, Integer>> priorityQueue = new PriorityQueue<>(
                (x,y) -> (y.getValue() - x.getValue())
            );
            for(Map.Entry<Long, Integer> entry : userIdToCountMap.entrySet()){
                priorityQueue.offer(entry);
            }
            List<Long> recommendationList = new ArrayList<>();
            for(int i=1; i<= CommonConstant.FOLLOW_RECOMMENDATION_LIST_SIZE; i++){
                if(!priorityQueue.isEmpty()){
                    recommendationList.add(priorityQueue.poll().getKey());
                } else break;
            }

            followRecommendationCacheRepository.updateRecommendationTargetUserIdList(recommendationList);

            // 캐시서버가 다운되었을 경우를 대비하여 DB에도 별도의 새로운 테이블을 만들어서 저장해 둔다.
            StringBuilder builder = new StringBuilder();
            for(Long id : recommendationList){
                builder.append(id);
                builder.append(",");
            }
            followRecommendationRepository.save(
                FollowRecommendation.builder()
                    .followRecommendationTargetUsers(builder.toString())
                    .build()
            );
        }

        List<Long> likeSampleList = likeSamplePoolCacheRepository.getSampleList();
        if(likeSampleList != null){
            // [ 주키 아이디 : 좋아요 샘플 횟수 카운트 ] 맵 구성.
            Map<Long, Integer> feedIdToCountMap = likeSampleList.stream().collect(
                Collectors.toMap(Function.identity(), e -> 1, Math::addExact)
            );

            // 맵 내용물 중에서 카운트 높은 횟수 100개(100개 미만일 경우 중간 브레이크) 골라내기.
            PriorityQueue<Entry<Long, Integer>> priorityQueue = new PriorityQueue<>(
                (x,y) -> (y.getValue() - x.getValue())
            );
            for(Map.Entry<Long, Integer> entry : feedIdToCountMap.entrySet()){
                priorityQueue.offer(entry);
            }

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
