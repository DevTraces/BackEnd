package com.devtraces.arterest.service.hashtag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.Hashtag;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @Mock
    private HashtagRepository hashtagRepository;
    @Mock
    private FeedHashtagMapRepository feedHashtagMapRepository;
    @InjectMocks
    private HashtagService hashtagService;

    @Test
    @DisplayName("해시태그 관련 정부 삭제 완료")
    void successDeleteHashtagRelatedData(){
        // given
        Feed feed = Feed.builder()
            .id(1L)
            .build();

        Hashtag hashtag = Hashtag.builder()
            .id(1L)
            .build();

        FeedHashtagMap feedHashtagMapEntity = FeedHashtagMap.builder()
            .id(1L)
            .hashtag(hashtag)
            .feed(feed)
            .build();

        List<FeedHashtagMap> feedHashtagMapList = new ArrayList<>();
        feedHashtagMapList.add(feedHashtagMapEntity);

        given(feedHashtagMapRepository.findByFeed(feed)).willReturn(feedHashtagMapList);
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(1L);

        // when
        hashtagService.deleteHashtagRelatedData(feed);

        // then
        verify(feedHashtagMapRepository, times(1)).findByFeed(any());
        verify(feedHashtagMapRepository, times(1)).deleteAllByFeedId(1L);
    }

}