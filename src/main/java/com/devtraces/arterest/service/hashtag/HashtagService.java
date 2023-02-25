package com.devtraces.arterest.service.hashtag;

import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final FeedHashtagMapRepository feedHashtagMapRepository;

    @Transactional
    public void deleteHashtagRelatedData(Feed feed){
        // 삭제될 FeedHashtagMap 데이터 목록을 가져옴.
        List<FeedHashtagMap> feedHashtagMapList = feedHashtagMapRepository.findByFeed(feed);

        // FeedHashtagMap 테이블에서 관련 정보 모두 삭제.
        feedHashtagMapRepository.deleteAllByFeedId(feed.getId());

        // 사용되지 않는 Hashtag 삭제.
        deleteNotUsingHashtag(feedHashtagMapList);
    }

    void deleteNotUsingHashtag(List<FeedHashtagMap> feedHashtagMapList){
        // 삭제된 FeedHashtagMap의 feedId에 매핑된 hashtagId가 더이상 FeedHashtagMap에 존재하지 않을 경우,
        // 해당 hastagId를 Hashtag 테이블에서 삭제함.
        for (FeedHashtagMap feedHashtagMap : feedHashtagMapList) {
            if(!feedHashtagMapRepository.existsByHashtag(feedHashtagMap.getHashtag())){
                hashtagRepository.deleteById(feedHashtagMap.getHashtag().getId());
            }
        }
    }
}
