package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.dto.feed.FeedResponse;
import com.devtraces.arterest.service.FeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ApiSuccessResponse<List<FeedResponse>> getFeedList(
        @RequestParam Long userId, @RequestParam int page
    ){
        return null;
    }

    @GetMapping("/{feedId}")
    public ApiSuccessResponse<FeedResponse> getOneFeed(@PathVariable Long feedId){
        return null;
    }

    @DeleteMapping("/{feedId}")
    public ApiSuccessResponse<?> deleteFeed(@PathVariable Long feedId){
        feedService.deleteFeed(feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
