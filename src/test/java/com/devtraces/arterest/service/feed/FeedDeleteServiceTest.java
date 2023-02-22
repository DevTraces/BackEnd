package com.devtraces.arterest.service.feed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class FeedDeleteServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private LikeNumberCacheRepository likeNumberCacheRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private FeedHashtagMapRepository feedHashtagMapRepository;
    @InjectMocks
    private FeedDeleteService feedDeleteService;

    @Test
    @DisplayName("피드 1개 제거")
    void successDeleteFeed(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .rereplyList(new ArrayList<>())
            .build();
        reply.getRereplyList().add(rereply);

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(new ArrayList<>())
            .user(user)
            .imageUrls("imageUrl,")
            .build();
        feed.getReplyList().add(reply);

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        doNothing().when(s3Service).deleteImage(anyString());
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(anyLong());
        doNothing().when(likeNumberCacheRepository).deleteLikeNumberInfo(anyLong());
        doNothing().when(likeRepository).deleteAllByFeedId(anyLong());
        doNothing().when(bookmarkRepository).deleteAllByFeedId(anyLong());
        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());
        doNothing().when(replyRepository).deleteAllByIdIn(anyList());
        doNothing().when(feedRepository).deleteById(anyLong());

        // when
        feedDeleteService.deleteFeed(1L, 1L);

        List<Long> longList = new ArrayList<>();
        longList.add(1L);

        // then
        verify(s3Service, times(1)).deleteImage("imageUrl");
        verify(feedHashtagMapRepository, times(1)).deleteAllByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).deleteLikeNumberInfo(1L);
        verify(likeRepository, times(1)).deleteAllByFeedId(1L);
        verify(bookmarkRepository, times(1)).deleteAllByFeedId(1L);
        verify(rereplyRepository, times(1)).deleteAllByIdIn(longList);
        verify(replyRepository, times(1)).deleteAllByIdIn(longList);
        verify(feedRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("게시물 삭제 실패 - 삭제 대상 게시물을 찾을 수 없음.")
    void failedDeleteFeedFeedNotFound(){
        // given
        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedDeleteService.deleteFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 삭제 실패 - 유저 정보가 게시물 작성자 정보와 일치하지 않음.")
    void failedDeleteFeedUserInfoNotMatch(){
        // given
        User user = User.builder()
            .id(2L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedDeleteService.deleteFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

}