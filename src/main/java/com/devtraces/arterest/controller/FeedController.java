package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.dto.feed.FeedResponse;
import com.devtraces.arterest.service.FeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        // 마법의 상수가 들어가 있음. 즉, 1 페이지에 포함될 요소의 개수를 팀에서 정해놔야 함.
        PageRequest pageRequest = PageRequest.of(page, 20);
        return ApiSuccessResponse.from(feedService.getFeedResponseList(userId, pageRequest));
    }

    @GetMapping("/{userId}/{feedId}")
    public ApiSuccessResponse<FeedResponse> getOneFeed(
        @PathVariable Long userId, @PathVariable Long feedId
    ){
        // 여기서도 유저 아이디가 필요하다.
        return ApiSuccessResponse.from(feedService.getOneFeed(userId, feedId));
    }

    @DeleteMapping("/{feedId}")
    public ApiSuccessResponse<?> deleteFeed(@PathVariable Long feedId){
        feedService.deleteFeed(feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
