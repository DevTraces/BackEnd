package com.devtraces.arterest.service.feed.application;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.service.hashtag.HashtagService;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedDeleteApplication {

    private final FeedRepository feedRepository;
    private final S3Service s3Service;
    private final HashtagService hashtagService;

    // TODO 스프링 @Async를 사용해서 비동기 멀티 스레딩으로 처리하면 응답지연시간 최소화 가능.
    @Transactional
    public void deleteFeed(Long userId, Long feedId){
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );
        if(!Objects.equals(feed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // S3에 올려놨던 사진들을 전부 삭제한다.
        if(!feed.getImageUrls().equals("")){
            for(String deleteTargetUrl : feed.getImageUrls().split(",")){
                s3Service.deleteImage(deleteTargetUrl);
            }
        }

        // 해시태그 관련 정보들을 전부 삭제한다.
        hashtagService.deleteHashtagRelatedData(feed);

        // 피드 삭제.
        feedRepository.deleteById(feedId);
    }

}
