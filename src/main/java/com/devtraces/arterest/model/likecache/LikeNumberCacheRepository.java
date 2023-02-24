package com.devtraces.arterest.model.likecache;

import com.devtraces.arterest.common.constant.CommonConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeNumberCacheRepository {

    private final RedisTemplate<String, String> template;

    // 좋아요 숫자를 레디스로부터 가져온다.
    public Long getFeedLikeNumber(Long feedId){
        try {
            String key = getKey(feedId);
            String value = template.opsForValue().get(key);
            return value == null ? null : Long.parseLong(value);
        } catch (Exception e){
            log.error("캐시서버에서 좋아요 개수 획득 실패.");
            return null;
        }
    }

    // 게시물이 처음 만들어졌을 때 기록을 위한 키밸류 쌍을 최초로 같이 기록한다.
    public void setInitialLikeNumber(Long feedId){
        try{
            String key = getKey(feedId);
            template.opsForValue().set(key, "0");
        } catch (Exception e){
            log.error("캐시서버에서 좋아요 개수 0으로 저장 실패.");
        }
    }

    public void setLikeNumber(Long feedId, Long likeNumber){
        try {
            String key = getKey(feedId);
            template.opsForValue().set(key, String.valueOf(likeNumber));
        } catch (Exception e){
            log.error("캐시서버에 좋아요 개수 저장 실패");
        }
    }

    // 좋아요 숫자를 += 1 한다.
    public void plusOneLike(Long feedId){
        try {
            String key = getKey(feedId);
            template.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("캐시서버에서 좋아요 개수 1 증가 기록 실패");
        }
    }

    // 좋아요 숫자를 -= 1 한다.
    public void minusOneLike(Long feedId){
        try {
            String key = getKey(feedId);
            template.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("캐시서버에서 좋아요 개수 1 감소 기록 실패");
        }
    }

    // 좋아요 숫자를 기록한 키-밸류 쌍을 삭제한다.
    public void deleteLikeNumberInfo(Long feedId){
        String key = getKey(feedId);
        template.delete(key);
    }

    private String getKey(Long feedId){
        return CommonConstant.REDIS_LIKE_NUMBER_KEY_PREFIX + feedId;
    }

}
