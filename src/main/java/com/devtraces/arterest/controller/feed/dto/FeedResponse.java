package com.devtraces.arterest.controller.feed.dto;

import com.devtraces.arterest.domain.feed.Feed;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedResponse {

    private Long feedId;

    private String authorProfileImageUrl;
    private String authorNickname;

    private String content;
    private List<String> imageUrls;
    private List<String> hashtags; // ["#감자", "#potato" ...]

    private Long numberOfLike;
    private Integer numberOfReply;
    private boolean isLiked; // 트루이면 좋아요 눌렀던 게시물인 것.
    private boolean isBookMarked;

    private LocalDateTime createdAt; // 프런트엔드 측에서는 "2023-02-07T09:59:23.653281"라는 문자열 받음.
    private LocalDateTime modifiedAt;

    public static FeedResponse from(
        Feed feed, Set<Long> likedFeedSet, Long numberOfLike, Set<Long> bookmarkedFeedSet
    ){
        return FeedResponse.builder()
            .feedId(feed.getId())
            .authorProfileImageUrl(feed.getUser().getProfileImageUrl())
            .authorNickname(feed.getUser().getNickname())
            .content(feed.getContent())
            .imageUrls(
                feed.getImageUrls().equals("") ? null :
                Arrays.stream(feed.getImageUrls().split(",")).collect(Collectors.toList())
            )
            .hashtags(
                feed.getFeedHashtagMapList() == null ? null :
                feed.getFeedHashtagMapList().stream().map(
                    feedHashtagMap -> feedHashtagMap.getHashtag().getHashtagString()
                ).collect(Collectors.toList())
            )
            .numberOfLike(numberOfLike)
            .numberOfReply(feed.getReplyList() == null ? 0 : feed.getReplyList().size())
            .isLiked(likedFeedSet == null ? false : likedFeedSet.contains(feed.getId()))
            .isBookMarked(
                bookmarkedFeedSet == null? false : bookmarkedFeedSet.contains(feed.getId())
            ) // 현재 게시물이 예전에 북마크 했던 게시물인지 여부
            .createdAt(feed.getCreatedAt())
            .modifiedAt(feed.getModifiedAt())
            .build();
    }

}
