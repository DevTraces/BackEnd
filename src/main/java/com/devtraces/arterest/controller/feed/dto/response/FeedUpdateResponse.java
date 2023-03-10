package com.devtraces.arterest.controller.feed.dto.response;

import com.devtraces.arterest.model.feed.Feed;
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

    public static FeedUpdateResponse from(Feed feed, List<String> hashtagStringList, String content){
        return FeedUpdateResponse.builder()
            .feedId(feed.getId())
            .content(content)
            .imageUrls(
                feed.getImageUrls() == null ? null :
                Arrays.stream(feed.getImageUrls().split(",")).collect(Collectors.toList())
            )
            .hashtags(hashtagStringList)
            .build();
    }
}
