package com.devtraces.arterest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.controller.reply.dto.ReplyRequest;
import com.devtraces.arterest.controller.reply.dto.ReplyResponse;
import com.devtraces.arterest.service.reply.ReplyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
class ReplyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;

    @InjectMocks
    private ReplyService replyService;

    @Test
    @DisplayName("댓글 1개 생성")
    void successCreateReply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .feed(feed)
            .build();

        ReplyRequest replyRequest = new ReplyRequest("댓글내용");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));
        given(replyRepository.save(any())).willReturn(reply);

        // when
        replyService.createReply(1L, 1L, replyRequest);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(feedRepository, times(1)).findById(anyLong());
        verify(replyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 제한 길이 1000자 초과.")
    void failedCreateReplyContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        ReplyRequest replyRequest = new ReplyRequest(sb.toString());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.createReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 유저 정보 확인 불가")
    void failedCreateReplyUserNotFound(){
        // given
        ReplyRequest replyRequest = new ReplyRequest("댓글 내용");

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.createReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 댓글이 달리게 될 게시물의 정보 확인 불가")
    void failedCreateReplyFeedNotFound(){
        // given
        ReplyRequest replyRequest = new ReplyRequest("댓글 내용");

        User user = User.builder()
            .id(1L)
            .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.createReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 리스트 읽기")
    void successGetReplyList(){
        // given

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageLink("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .authorId(2L)
            .imageLinks("url2,url3")
            .hashtags("#h1,#h2,#h3")
            .user(user)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .feed(feed)
            .content("this is reply")
            .build();

        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

        Slice<Reply> slice = new PageImpl<>(replyList);

        given(replyRepository.findAllByFeedId(1L, PageRequest.of(0, 10))).willReturn(slice);

        // when
        List<ReplyResponse> replyResponseList = replyService.getReplyList(1L, 0, 10);

        // then
        verify(replyRepository, times(1)).findAllByFeedId(1L, PageRequest.of(0, 10));
        assertEquals(replyResponseList.size(), 1);
    }

    @Test
    @DisplayName("댓글 1개 수정")
    void successUpdateReply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        Reply replyBeforeUpdate = Reply.builder()
            .id(1L)
            .user(user)
            .content("댓글내용")
            .feed(feed)
            .build();

        Reply replyAfterUpdate = Reply.builder()
            .id(1L)
            .user(user)
            .content("수정댓글내용")
            .feed(feed)
            .build();

        ReplyRequest replyRequest = new ReplyRequest("수정댓글내용");

        given(replyRepository.findById(anyLong())).willReturn(Optional.of(replyBeforeUpdate));
        given(replyRepository.save(any())).willReturn(replyAfterUpdate);

        // when
        replyService.updateReply(1L, 1L, replyRequest);

        // then
        verify(replyRepository, times(1)).findById(anyLong());
        verify(replyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 제한 길이 1000자 초과.")
    void failedUpdateReplyContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        ReplyRequest replyRequest = new ReplyRequest(sb.toString());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.updateReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 유저 정보 불일치")
    void failedUpdateReplyUserInfoNotMatch(){
        // given
        ReplyRequest replyRequest = new ReplyRequest("댓글 내용");

        User user = User.builder()
            .id(2L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .content("원래 댓글 내용")
            .build();

        given(replyRepository.findById(1L)).willReturn(Optional.of(reply));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.updateReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 수정 대상 댓글 찾지 못함.")
    void failedUpdateReplyReplyNotFound(){
        // given
        ReplyRequest replyRequest = new ReplyRequest("댓글 내용");

        given(replyRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.updateReply(1L, 1L, replyRequest)
        );

        // then
        assertEquals(ErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 1개 삭제")
    void deleteReply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .build();

        List<Rereply> rereplyList = new ArrayList<>();
        rereplyList.add(rereply);

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .content("댓글내용")
            .feed(feed)
            .rereplyList(rereplyList)
            .build();

        given(replyRepository.findById(anyLong())).willReturn(Optional.of(reply));
        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());

        // when
        replyService.deleteReply(1L, 1L);

        // then
        verify(replyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).deleteAllByIdIn(anyList());
        verify(replyRepository, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 수정 대상 댓글 찾지 못함.")
    void failedDeleteReplyReplyNotFound(){
        // given
        given(replyRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.deleteReply(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 수정 대상 댓글 찾지 못함.")
    void failedDeleteUserInfoNotMatch(){
        // given
        User user = User.builder()
            .id(2L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .content("원래 댓글 내용")
            .build();

        given(replyRepository.findById(1L)).willReturn(Optional.of(reply));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> replyService.deleteReply(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

}