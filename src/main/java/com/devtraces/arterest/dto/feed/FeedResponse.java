package com.devtraces.arterest.dto.feed;

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
import org.springframework.stereotype.Service;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedResponse {

    private Long feedId;

    private Long authorId;
    private String authorProfileImageUrl;
    private String authorNickname;

    private String content;
    private List<String> imageUrls;
    private List<String> hashtags; // ["#감자", "#potato", "#이얍이얍"]

    private Long numberOfLike;
    private Integer numberOfReply;
    private boolean isLiked; // 트루이면 좋아요 눌렀던 게시물인 것.

    private LocalDateTime createdAt; // 프런트엔드 측에서는 "2023-02-07T09:59:23.653281"라는 문자열 받음.
    private LocalDateTime modifiedAt;

    public static FeedResponse from(Feed feed, Set<Long> likedFeedSet, Long numberOfLike){
        return FeedResponse.builder()
            .feedId(feed.getId())
            .authorId(feed.getAuthorId())
            .authorProfileImageUrl(feed.getUser().getProfileImageLink())
            .authorNickname(feed.getUser().getNickname())
            .content(feed.getContent())
            .imageUrls(Arrays.stream(feed.getImageLinks().split(","))
                .collect(Collectors.toList()))
            .hashtags(Arrays.stream(feed.getHashtags().split(","))
                .collect(Collectors.toList()))
            .numberOfLike(numberOfLike)
            .numberOfReply(feed.getReplyList().size())
            .isLiked(likedFeedSet.contains(feed.getId()))
            .createdAt(feed.getCreatedAt())
            .modifiedAt(feed.getModifiedAt())
            .build();
    }

}
