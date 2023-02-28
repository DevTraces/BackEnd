package com.devtraces.arterest.service.like;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.like.dto.response.LikeResponse;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

import com.devtraces.arterest.service.notice.NoticeService;
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
    private final FeedRepository feedRepository;
    private final NoticeService noticeService;

    @Transactional
    public void pressLikeOnFeed(Long userId, Long feedId) {
        validateFeedExistence(feedId);
        if(!likeRepository.existsByUserIdAndFeedId(userId, feedId)){
            // 중복 좋아요가 아닌 경우에만 실제 좋아요 요청이 처리 됨.
            // 예외를 던지지 않게 함.
            likeRepository.save(
                Likes.builder()
                    .feedId(feedId)
                    .userId(userId)
                    .build()
            );
            likeNumberCacheRepository.plusOneLike(feedId);

            // 피드 주인에게 알림 생성
            noticeService.createLikeNotice(userId, feedId);
        }
    }

    // TODO 좋아요 취소에 대해서는 따로 알림을 보낼 필요가 없다고 생각됨.
    @Transactional
    public void cancelLikeOnFeed(Long userId, Long feedId) {
        validateFeedExistence(feedId);
        Long prevLikeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedId);
        if (prevLikeNumber == null) { prevLikeNumber = likeRepository.countByFeedId(feedId); }
        if (prevLikeNumber.equals(0L)) { throw BaseException.LIKE_NUMBER_BELLOW_ZERO; }

        likeRepository.deleteByUserIdAndFeedId(userId, feedId);

        likeNumberCacheRepository.minusOneLike(feedId);
    }

    @Transactional(readOnly = true)
    public List<LikeResponse> getLikedUserList(
        Long feedId, int page, int pageSize
    ) {
        validateFeedExistence(feedId);
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        List<Long> likedUserIdList = likeRepository
            .findAllByFeedIdOrderByCreatedAtDesc(feedId, pageRequest)
            .getContent().stream().map(Likes::getUserId).collect(Collectors.toList());

        return userRepository.findAllByIdIn(likedUserIdList).stream()
            .map(LikeResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void deleteLikeRelatedData(Long feedId){
        likeNumberCacheRepository.deleteLikeNumberInfo(feedId);
        likeRepository.deleteAllByFeedId(feedId);
    }

    private void validateFeedExistence(Long feedId) {
        if(!feedRepository.existsById(feedId)){
            throw BaseException.FEED_NOT_FOUND;
        }
    }
}