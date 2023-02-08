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
@Service
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedResponse {

    private Long feedId;

    private Long authorId;
    private String authorProfileImageLink;
    private String authorNickname;

    private String content;
    private List<String> imageLinks;
    private List<String> hashtags; // ["#감자", "#potato", "#이얍이얍"]

    private Long numberOfLike;
    private Integer numberOfReply;
    private boolean likeBefore; // 트루이면 좋아요 눌렀던 게시물인 것.

    private LocalDateTime createdAt; // 프런트엔드 측에서는 "2023-02-07T09:59:23.653281"라는 문자열 받음.
    private LocalDateTime modifiedAt;

    public static FeedResponse from(Feed feed, Set<Long> likedFeedSet){
        return FeedResponse.builder()
            .feedId(feed.getId())
            .authorId(feed.getAuthorId())
            .authorProfileImageLink(feed.getUser().getProfileImageLink())
            .authorNickname(feed.getUser().getNickname())
            .content(feed.getContent())
            .imageLinks(Arrays.stream(feed.getImageLinks().split(","))
                .collect(Collectors.toList()))
            .hashtags(Arrays.stream(feed.getHashtags().split(","))
                .collect(Collectors.toList()))
            // 좋아요 개수는 이 메서드 밖에서 별도로 처리한다.
            .numberOfReply(feed.getReplyList().size())
            .likeBefore(likedFeedSet.contains(feed.getId()))
            .createdAt(feed.getCreatedAt())
            .modifiedAt(feed.getModifiedAt())
            .build();
    }

}
