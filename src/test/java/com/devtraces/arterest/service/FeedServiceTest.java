package com.devtraces.arterest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
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
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    @DisplayName("피드 1개 제거")
    void success_delete_feed(){

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

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());
        doNothing().when(replyRepository).deleteAllByIdIn(anyList());
        doNothing().when(feedRepository).deleteById(anyLong());

        // when
        feedService.deleteFeed(1L);

        List<Long> longList = new ArrayList<>();
        longList.add(1L);

        // then
        verify(rereplyRepository, times(1)).deleteAllByIdIn(longList);
        verify(replyRepository, times(1)).deleteAllByIdIn(longList);
        verify(feedRepository).deleteById(anyLong());
    }

}