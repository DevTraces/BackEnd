package com.devtraces.arterest.service.like;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.like.dto.response.LikeResponse;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final LikeNumberCacheRepository likeNumberCacheRepository;

    @Transactional
    public void pressLikeOnFeed(Long userId, Long feedId) {
        // 유니크키 제약조건이 이미 Likes 테이블에 걸려 있지만, 이것만으로는
        // FE에 "중복 좋아요"라는 예외 메시지를 전달하지 못하고 그저 인터널 서버 에러라고만 뜸.
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

    // TODO 좋아요 취소에 대해서는 따로 알림을 보낼 필요가 없다고 생각됨.
    @Transactional
    public void cancelLikeOnFeed(Long userId, Long feedId) {
        Long prevLikeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedId);
        if (prevLikeNumber == null) { prevLikeNumber = likeRepository.countByFeedId(feedId); }
        if (prevLikeNumber.equals(0L)) { throw BaseException.LIKE_NUMBER_BELLOW_ZERO; }

        likeRepository.deleteByUserIdAndFeedId(userId, feedId);

        likeNumberCacheRepository.minusOneLike(feedId);
    }

    // TODO : AuthenticationPrincipal userId가 실제 좋아요 누른 유저 리스트 가져오기 API에서 사용되지 않음.
    public List<LikeResponse> getLikedUserList(
        Long userId, Long feedId, int page, int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        List<Long> likedUserIdList = likeRepository
            .findAllByFeedIdOrderByCreatedAtDesc(feedId, pageRequest)
            .getContent().stream().map(Likes::getUserId).collect(Collectors.toList());

        return userRepository.findAllByIdIn(likedUserIdList).stream().map(LikeResponse::from)
            .collect(Collectors.toList());
    }
}