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
import com.devtraces.arterest.domain.like.LikeRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.dto.feed.FeedResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    @DisplayName("피드 1개 읽기")
    void successGetOneFeed(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

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
            .replyList(replyList)
            .imageLinks("url2,url3")
            .hashtags("#h1,#h2,#h3")
            .user(user)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        given(likeRepository.countByFeedId(1L)).willReturn(0L);

        //when
        FeedResponse feedResponse = feedService.getOneFeed(2L, 1L);

        //then
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(feedRepository, times(1)).findById(1L);
        assertEquals(feedResponse.getFeedId(), 1L);
    }

    @Test
    @DisplayName("피드 리스트 읽기")
    void successGetFeedList(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

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
            .replyList(replyList)
            .imageLinks("url2,url3")
            .hashtags("#h1,#h2,#h3")
            .user(user)
            .build();

        List<Feed> feedList = new ArrayList<>();
        feedList.add(feed);

        Slice<Feed> slice = new PageImpl<>(feedList);

        given(feedRepository.findAllByAuthorId(1L, PageRequest.of(0, 10))).willReturn(slice);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);

        //when
        List<FeedResponse> feedResponseList = feedService.getFeedResponseList(1L, PageRequest.of(0, 10));

        //then
        verify(feedRepository, times(1)).findAllByAuthorId(1L, PageRequest.of(0, 10));
        assertEquals(feedResponseList.size(), 1);
    }

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
            .build();
        feed.getReplyList().add(reply);

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        doNothing().when(likeRepository).deleteAllByFeedId(anyLong());
        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());
        doNothing().when(replyRepository).deleteAllByIdIn(anyList());
        doNothing().when(feedRepository).deleteById(anyLong());

        // when
        feedService.deleteFeed(1L, 1L);

        List<Long> longList = new ArrayList<>();
        longList.add(1L);

        // then
        verify(likeRepository, times(1)).deleteAllByFeedId(1L);
        verify(rereplyRepository, times(1)).deleteAllByIdIn(longList);
        verify(replyRepository, times(1)).deleteAllByIdIn(longList);
        verify(feedRepository).deleteById(anyLong());
    }

}