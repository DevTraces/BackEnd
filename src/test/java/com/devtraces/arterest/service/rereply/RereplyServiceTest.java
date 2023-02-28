package com.devtraces.arterest.service.rereply;

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
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.controller.rereply.dto.request.RereplyRequest;
import com.devtraces.arterest.controller.rereply.dto.response.RereplyResponse;
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
class RereplyServiceTest {

    @Mock
    private RereplyRepository rereplyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReplyRepository replyRepository;

    @InjectMocks
    private RereplyService rereplyService;

    @Test
    @DisplayName("대댓글 1개 생성")
    void successCreateRereply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .build();

        RereplyRequest rereplyRequest = new RereplyRequest("댓글내용");

        Rereply rereply = Rereply.builder()
            .user(user)
            .id(1L)
            .reply(reply)
            .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(replyRepository.findById(anyLong())).willReturn(Optional.of(reply));
        given(rereplyRepository.save(any())).willReturn(rereply);

        // when
        rereplyService.createRereply(1L, 1L, 1L, rereplyRequest);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(replyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 제한 길이 초과")
    void failedCreateRereplyContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        RereplyRequest rereplyRequest = new RereplyRequest(sb.toString());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.createRereply(1L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 유저 정보 확인 불가")
    void failedCreateRereplyUserNotFound(){
        // given
        RereplyRequest rereplyRequest = new RereplyRequest("댓글 내용");

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.createRereply(1L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 대댓글이 달리게 될 댓글 정보 찾지 못함.")
    void failedCreateRereplyRereplyNotFound(){
        // given
        RereplyRequest rereplyRequest = new RereplyRequest("댓글 내용");

        User user = User.builder()
            .id(1L)
            .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.createRereply(1L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 리스트 읽기")
    void successGetRereplyList(){
        // given
        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .user(user)
            .content("this is reply")
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .reply(reply)
            .build();

        List<Rereply> rereplyList = new ArrayList<>();
        rereplyList.add(rereply);

        Slice<Rereply> slice = new PageImpl<>(rereplyList);

        given(rereplyRepository.findAllByReplyId(1L, PageRequest.of(0, 10))).willReturn(slice);

        // when
        List<RereplyResponse> rereplyResponseList = rereplyService.getRereplyList(1L, 1L, 0, 10);

        // then
        verify(rereplyRepository, times(1)).findAllByReplyId(1L, PageRequest.of(0, 10));
        assertEquals(rereplyResponseList.size(), 1);
    }

    @Test
    @DisplayName("대댓글 1개 수정")
    void successUpdateRereply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .build();

        Rereply rereplyBeforeUpdate = Rereply.builder()
            .id(1L)
            .user(user)
            .reply(reply)
            .content("대댓글내용")
            .build();

        Rereply rereplyAfterUpdate = Rereply.builder()
            .id(1L)
            .user(user)
            .reply(reply)
            .content("수정후대댓글내용")
            .build();

        RereplyRequest rereplyRequest = new RereplyRequest("수정대댓글내용");

        given(rereplyRepository.findById(anyLong())).willReturn(Optional.of(rereplyBeforeUpdate));
        given(rereplyRepository.save(any())).willReturn(rereplyAfterUpdate);

        // when
        rereplyService.updateRereply(1L, 1L, 1L, rereplyRequest);

        // then
        verify(rereplyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("대댓글 1개 수정 실패 - 입력 제한 길이 초과")
    void failedUpdateRereplyContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        RereplyRequest rereplyRequest = new RereplyRequest(sb.toString());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.updateRereply(1L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 1개 수정 실패 - 수정 대상 대댓글 찾지 못함.")
    void failedUpdateRereplyRereplyNotFound(){
        // given
        RereplyRequest rereplyRequest = new RereplyRequest("수정 대댓글");

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.updateRereply(1L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.REREPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 1개 수정 실패 - 유저 정보 불일치")
    void failedUpdateRereplyUserInfoNotMatch(){
        // given
        RereplyRequest rereplyRequest = new RereplyRequest("수정 대댓글");

        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .build();

        given(rereplyRepository.findById(1L)).willReturn(Optional.of(rereply));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.updateRereply(2L, 1L, 1L, rereplyRequest)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 1개 삭제")
    void successDeleteRereply(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .build();

        given(rereplyRepository.findById(anyLong())).willReturn(Optional.of(rereply));
        doNothing().when(rereplyRepository).deleteById(anyLong());

        // when
        rereplyService.deleteRereply(1L, 1L);

        // then
        verify(rereplyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("대댓글 삭제 실패 - 삭제 대상 대댓글 찾지 못함.")
    void failedDeleteRereplyRereplyNotFound(){
        // given
        given(rereplyRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.deleteRereply(2L, 1L)
        );

        // then
        assertEquals(ErrorCode.REREPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 삭제 실패 - 유저 정보 불일치.")
    void failedDeleteRereplyUserInfoNotMatch(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .build();

        given(rereplyRepository.findById(1L)).willReturn(Optional.of(rereply));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> rereplyService.deleteRereply(2L, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("피드 관련 대댓글들 삭제 성공")
    void successDeleteAllFeedRelatedRereply(){
        // given
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
            .build();

        feed.getReplyList().add(reply);

        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());

        // when
        rereplyService.deleteAllFeedRelatedRereply(feed);

        // then
        verify(rereplyRepository, times(1)).deleteAllByIdIn(anyList());
    }

}