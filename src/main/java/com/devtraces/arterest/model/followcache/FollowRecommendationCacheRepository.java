package com.devtraces.arterest.model.followcache;

import static com.devtraces.arterest.common.constant.CommonUtil.getLongListFromCacheServer;

import com.devtraces.arterest.common.constant.CommonConstant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowRecommendationCacheRepository {

    private final RedisTemplate<String, String> template;
    private final String key = CommonConstant.REDIS_FOLLOW_RECOMMENDATION_LIST_KEY;

    public void updateRecommendationTargetUserIdList(List<Long> recommendationList) {
        try {
            template.delete(key);
            for(Long id : recommendationList){
                template.opsForList().rightPush(key, String.valueOf(id));
            }
        } catch (Exception e) {
            log.error("팔로우 추천 타깃 유저 리스트 캐시 실패.");
        }
    }

    // 팔로우 추천 타깃 유저 리스트를 리턴한다.
    public List<Long> getFollowTargetUserIdList(){
        try {
            return getLongListFromCacheServer(template, key);
        } catch (Exception e){
            log.error("레디스로부터 팔로우 추천 타깃 유저 리스트 획득 실패");
            return null;
        }
    }

}
