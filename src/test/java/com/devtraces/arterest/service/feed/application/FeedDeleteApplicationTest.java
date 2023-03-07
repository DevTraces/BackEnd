package com.devtraces.arterest.service.feed.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.service.bookmark.BookmarkService;
import com.devtraces.arterest.service.feed.FeedDeleteService;
import com.devtraces.arterest.service.feed.FeedReadService;
import com.devtraces.arterest.service.hashtag.HashtagService;
import com.devtraces.arterest.service.like.LikeService;
import com.devtraces.arterest.service.notice.NoticeService;
import com.devtraces.arterest.service.reply.ReplyService;
import com.devtraces.arterest.service.rereply.RereplyService;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FeedDeleteApplicationTest {

    @Mock
    private FeedDeleteService feedDeleteService;
    @Mock
    private FeedReadService feedReadService;
    @Mock
    private S3Service s3Service;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private LikeService likeService;
    @Mock
    private BookmarkService bookmarkService;
    @Mock
    private RereplyService rereplyService;
    @Mock
    private ReplyService replyService;
    @Mock
    private NoticeService noticeService;
    @InjectMocks
    private FeedDeleteApplication feedDeleteApplication;

    @Test
    @DisplayName("게시물 1개 삭제 성공")
    void successDeleteFeed(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Feed deleteTargetFeed = Feed.builder()
            .id(1L)
            .user(user)
            .imageUrls("imageUrl1")
            .build();

        given(feedReadService.getOneFeedEntity(1L)).willReturn(deleteTargetFeed);
        doNothing().when(s3Service).deleteImage(anyString());
        doNothing().when(hashtagService).deleteHashtagRelatedData(any());
        doNothing().when(likeService).deleteLikeRelatedData(1L);
        doNothing().when(bookmarkService).deleteAllFeedRelatedBookmark(1L);
        doNothing().when(rereplyService).deleteAllFeedRelatedRereply(any());
        doNothing().when(replyService).deleteAllFeedRelatedReply(any());
        doNothing().when(noticeService).deleteNoticeWhenFeedDeleted(anyLong());
        doNothing().when(feedDeleteService).deleteFeedEntity(1L);

        // when
        feedDeleteApplication.deleteFeed(1L, 1L);

        // then
        verify(feedReadService, times(1)).getOneFeedEntity(1L);
        verify(s3Service, times(1)).deleteImage(anyString());
        verify(hashtagService, times(1)).deleteHashtagRelatedData(any());
        verify(likeService, times(1)).deleteLikeRelatedData(1L);
        verify(bookmarkService, times(1)).deleteAllFeedRelatedBookmark(1L);
        verify(rereplyService, times(1)).deleteAllFeedRelatedRereply(any());
        verify(replyService, times(1)).deleteAllFeedRelatedReply(any());
        verify(noticeService, times(1)).deleteNoticeWhenFeedDeleted(anyLong());
        verify(feedDeleteService, times(1)).deleteFeedEntity(1L);
    }

    @Test
    @DisplayName("게시물 1개 삭제 실패 - 유저 정보 불일치")
    void failedDeleteFeedUserInfoNotMatch(){
        User user = User.builder()
            .id(1L)
            .build();

        Feed deleteTargetFeed = Feed.builder()
            .id(1L)
            .user(user)
            .imageUrls("imageUrl1")
            .build();

        given(feedReadService.getOneFeedEntity(1L)).willReturn(deleteTargetFeed);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedDeleteApplication.deleteFeed(2L, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

}