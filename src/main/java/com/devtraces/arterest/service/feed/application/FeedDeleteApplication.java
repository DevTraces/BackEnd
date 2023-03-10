package com.devtraces.arterest.service.feed.application;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.service.bookmark.BookmarkService;
import com.devtraces.arterest.service.feed.FeedDeleteService;
import com.devtraces.arterest.service.feed.FeedReadService;
import com.devtraces.arterest.service.hashtag.HashtagService;
import com.devtraces.arterest.service.like.LikeService;
import com.devtraces.arterest.service.notice.NoticeService;
import com.devtraces.arterest.service.reply.ReplyService;
import com.devtraces.arterest.service.rereply.RereplyService;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedDeleteApplication {

    private final FeedDeleteService feedDeleteService;
    private final FeedReadService feedReadService;
    private final S3Service s3Service;
    private final HashtagService hashtagService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final RereplyService rereplyService;
    private final ReplyService replyService;
    private final NoticeService noticeService;
    
    @Transactional
    public void deleteFeed(Long userId, Long feedId){
        Feed deleteTargetFeed = feedReadService.getOneFeedEntity(feedId);

        if(!Objects.equals(deleteTargetFeed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // S3에 올려놨던 사진들을 전부 삭제한다.
        if(!deleteTargetFeed.getImageUrls().equals("")){
            for(String deleteTargetUrl : deleteTargetFeed.getImageUrls().split(",")){
                s3Service.deleteImage(deleteTargetUrl);
            }
        }

        // 해시태그 관련 정보들을 전부 삭제한다.
        hashtagService.deleteHashtagRelatedData(deleteTargetFeed);

        // 좋아요 관련 정보들을 전부 삭제한다.
        likeService.deleteLikeRelatedData(feedId);

        // 북마크 테이블에서 정보 모두 삭제.
        bookmarkService.deleteAllFeedRelatedBookmark(feedId);

        // 대댓글 삭제
        rereplyService.deleteAllFeedRelatedRereply(deleteTargetFeed);

        // 댓글 삭제
        replyService.deleteAllFeedRelatedReply(deleteTargetFeed);

        // 삭제하는 피드와 관련된 알림 삭제
        noticeService.deleteNoticeWhenFeedDeleted(feedId);

        // 마지막으로 피드 삭제
        feedDeleteService.deleteFeedEntity(feedId);
    }
}
