package com.devtraces.arterest.model.followcache;

import static com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository.getLongs;

import com.devtraces.arterest.common.constant.CommonConstant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowSamplePoolCacheRepository {

    private final RedisTemplate<String, String> template;
    private final String key = CommonConstant.REDIS_FOLLOW_SAMPLE_POOL_LIST_KEY;

    // 새로운 팔로우 정보 표본을 저장한다.
    // 리스트의 길이를 600개 이내로 유지한다.
    public void pushSample(Long followTargetUserId) {
        try {
            template.opsForList().rightPush(key, String.valueOf(followTargetUserId));
            Long size = template.opsForList().size(key);
            if(size != null && size > CommonConstant.REDIS_FOLLOW_SAMPLE_POOL_LIST_SIZE){
                template.opsForList().leftPop(key);
            }
        } catch (Exception e) {
            log.error("팔로우 샘플 레디스 리스트에 푸시 실패");
        }
    }

    // 레디스에 저장돼 있는 리스트를 반환한다.
    public List<Long> getSampleList(){
        try {
            return getLongs(template, key);
        } catch (Exception e){
            log.error("레디스로부터 팔로우 샘플 리스트 획득 실패");
            return null;
        }
    }

}
