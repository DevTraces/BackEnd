package com.devtraces.arterest.service.like;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeNumberCacheRepository likeNumberCacheRepository;

    public void pressLikeOnFeed(Long userId, Long feedId) {
        // 유니크키 제약조건이 이미 Likes 테이블에 걸려 있지만, 이것만으로는
        // FE에 "중복 좋아요"라는 예외 메시지를 전달하지 못하고
        // 그저 인터널 서버 에러라고만 뜸.
        // TODO 중복 좋아요 및 팔로우에 대한 적절한 예외처리 방법 결정후 수정 필요.
        if(likeRepository.existsByUserIdAndFeedId(userId, feedId)){
            throw BaseException.DUPLICATED_FOLLOW_OR_LIKE;
        }

        likeRepository.save(
            Likes.builder()
                .feedId(feedId)
                .userId(userId)
                .build()
        );

        likeNumberCacheRepository.plusOneLike(feedId);
    }
}
