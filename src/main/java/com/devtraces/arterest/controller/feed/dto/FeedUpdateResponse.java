package com.devtraces.arterest.controller.feed.dto;

import com.devtraces.arterest.domain.feed.Feed;
import java.util.Arrays;
import java.util.List;
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
public class FeedUpdateResponse {
    private Long feedId;
    private String content;
    private List<String> imageUrls;
    private List<String> hashtags;

    public static FeedUpdateResponse from(Feed feed){
        return FeedUpdateResponse.builder()
            .feedId(feed.getId())
            .content(feed.getContent() == null ? "" : feed.getContent())
            .imageUrls(
                Arrays.stream(feed.getImageUrls().split(",")).collect(Collectors.toList())
            )
            .hashtags(
                Arrays.stream(feed.getHashtags().split(",")).collect(Collectors.toList())
            )
            .build();
    }
}
