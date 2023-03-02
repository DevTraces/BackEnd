package com.devtraces.arterest.controller.feed;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.feed.dto.response.FeedCreateResponse;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.controller.feed.dto.response.FeedUpdateResponse;
import com.devtraces.arterest.service.feed.FeedDeleteService;
import com.devtraces.arterest.service.feed.FeedReadService;
import com.devtraces.arterest.service.feed.FeedService;
import com.devtraces.arterest.service.feed.application.FeedDeleteApplication;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    private final FeedReadService feedreadService;
    private final FeedDeleteApplication feedDeleteApplication;

    @PostMapping
    public ApiSuccessResponse<FeedCreateResponse> createFeed(
        @AuthenticationPrincipal Long userId,
        @RequestParam(value = "content", required = false) String content,
        @RequestParam(value = "imageFiles") List<MultipartFile> imageFileList,
        @RequestParam(value = "hashtags", required = false) List<String> hashtagList
    ){
        return ApiSuccessResponse.from(
            feedService.createFeed(userId, content, imageFileList, hashtagList)
        );
    }

    @GetMapping("/list/{nickname}")
    public ApiSuccessResponse<List<FeedResponse>> getFeedList(
        @AuthenticationPrincipal Long userId,
        @PathVariable String nickname,
        @RequestParam int page,
        @RequestParam(required = false, defaultValue = "10") int pageSize
    ){
        return ApiSuccessResponse.from(
            feedreadService.getFeedResponseList(userId, nickname, page, pageSize)
        );
    }

    @GetMapping("/{feedId}")
    public ApiSuccessResponse<FeedResponse> getOneFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        return ApiSuccessResponse.from(feedreadService.getOneFeed(userId, feedId));
    }

    @PostMapping("/{feedId}")
    public ApiSuccessResponse<FeedUpdateResponse> updateFeed(
        @AuthenticationPrincipal Long userId,
        @RequestParam(value = "content", required = false) String content,
        @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFileList,
        @RequestParam(value = "hashtags", required = false) List<String> hashtagList,
        @RequestParam(value = "existingUrlList", required = false) List<String> prevImageUrlList,
        @RequestParam(value = "indexList", required = false) List<String> indexList,
        @PathVariable Long feedId
    ){
        return ApiSuccessResponse.from(
            feedService.updateFeed(
                userId, content, imageFileList, hashtagList, prevImageUrlList, indexList, feedId
            )
        );
    }

    @DeleteMapping("/{feedId}")
    public ApiSuccessResponse<?> deleteFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        feedDeleteApplication.deleteFeed(userId, feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

    @GetMapping("/main")
    public ApiSuccessResponse<List<FeedResponse>> getMainFeedList(
        @AuthenticationPrincipal Long userId,
        @RequestParam Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        return ApiSuccessResponse.from(
            feedreadService.getMainFeedList(userId, page, pageSize)
        );
    }

}
