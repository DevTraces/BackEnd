package com.devtraces.arterest.model.followcache;

import com.devtraces.arterest.common.constant.CommonConstant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowRecommendCacheRepository {

    private final RedisTemplate<String, String> template;
    private final String key = CommonConstant.REDIS_FOLLOW_RECOMMENDATION_LIST_KEY;

    // DB로부터 특정 유저가 팔로우한 사람들의 주키 아이디 값 리스트를 받아서 레디스에 저장한다.
    public void setRecommendationTargetUserIdList(List<Long> recommendationList) {
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
            return getLongs(template, key);
        } catch (Exception e){
            log.error("레디스로부터 팔로우 추천 타깃 유저 리스트 획득 실패");
            return null;
        }
    }
    static List<Long> getLongs(RedisTemplate<String, String> template, String key) {
        Long size = template.opsForList().size(key);
        if(size != null){
            List<String> listFromRedis = template.opsForList().range(key, 0, size);
            assert listFromRedis != null;
            return listFromRedis.stream().map(Long::valueOf).collect(Collectors.toList());
        } else {
            return null;
        }
    }

}
