package com.devtraces.arterest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.dto.reply.ReplyRequest;
import com.devtraces.arterest.dto.reply.ReplyResponse;
import com.devtraces.arterest.dto.rereply.RereplyRequest;
import com.devtraces.arterest.dto.rereply.RereplyResponse;
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
        //given
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

        //when
        rereplyService.createRereply(1L, 1L, 1L, rereplyRequest);

        //then
        verify(userRepository, times(1)).findById(anyLong());
        verify(replyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("대댓글 리스트 읽기")
    void successGetRereplyList(){
        //given
        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageLink("url1")
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

        //when
        List<RereplyResponse> rereplyResponseList = rereplyService.getRereplyList(1L, 1L, 0, 10);

        //then
        verify(rereplyRepository, times(1)).findAllByReplyId(1L, PageRequest.of(0, 10));
        assertEquals(rereplyResponseList.size(), 1);
    }

    @Test
    @DisplayName("대댓글 1개 수정")
    void successUpdateRereply(){
        //given
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

        //when
        rereplyService.updateRereply(1L, 1L, 1L, rereplyRequest);

        //then
        verify(rereplyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("대댓글 1개 삭제")
    void successDeleteRereply(){
        //given
        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .user(user)
            .build();

        given(rereplyRepository.findById(anyLong())).willReturn(Optional.of(rereply));
        doNothing().when(rereplyRepository).deleteById(anyLong());

        //when
        rereplyService.deleteRereply(1L, 1L);

        //then
        verify(rereplyRepository, times(1)).findById(anyLong());
        verify(rereplyRepository, times(1)).deleteById(anyLong());
    }

}