package com.devtraces.arterest.controller.feed.dto.request;

import com.devtraces.arterest.controller.feed.dto.ExistingImageUrlDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedUpdateRequest {
    private String content;
    private List<String> hashtags;
    private List<ExistingImageUrlDto> existingImageUrls;
}
