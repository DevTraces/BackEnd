package com.devtraces.arterest.controller.feed;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.feed.dto.FeedCreateResponse;
import com.devtraces.arterest.controller.feed.dto.FeedResponse;
import com.devtraces.arterest.controller.feed.dto.FeedUpdateResponse;
import com.devtraces.arterest.service.feed.FeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PostMapping
    public ApiSuccessResponse<FeedCreateResponse> createFeed(
        @AuthenticationPrincipal Long userId,
        @RequestParam("content") String content,
        @RequestParam(value = "imageFiles") List<MultipartFile> imageFileList,
        @RequestParam(value = "hashtags", required = false) List<String> hashtagList
    ){
        return ApiSuccessResponse.from(
            feedService.createFeed(userId, content, imageFileList, hashtagList)
        );
    }

    @GetMapping("/{nickname}")
    public ApiSuccessResponse<List<FeedResponse>> getFeedList(
        @AuthenticationPrincipal Long userId,
        @RequestParam int page,
        @RequestParam(required = false, defaultValue = "10") int pageSize
    ){
        return ApiSuccessResponse.from(feedService.getFeedResponseList(userId, page, pageSize));
    }

    @GetMapping("/{feedId}")
    public ApiSuccessResponse<FeedResponse> getOneFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        return ApiSuccessResponse.from(feedService.getOneFeed(userId, feedId));
    }

    @PutMapping("/{feedId}")
    public ApiSuccessResponse<FeedUpdateResponse> updateFeed(
        @AuthenticationPrincipal Long userId,
        @RequestParam("content") String content,
        @RequestParam(value = "imageFiles") List<MultipartFile> imageFileList,
        @RequestParam(value = "hashtags", required = false) List<String> hashtagList,
        @PathVariable Long feedId
    ){
        return ApiSuccessResponse.from(
            feedService.updateFeed(userId, content, imageFileList, hashtagList, feedId)
        );
    }

    @DeleteMapping("/{feedId}")
    public ApiSuccessResponse<?> deleteFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        feedService.deleteFeed(userId, feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}