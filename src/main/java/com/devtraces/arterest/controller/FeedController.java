package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.dto.feed.FeedResponse;
import com.devtraces.arterest.service.FeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //JWT에서 userId 가져오게 될 경우, 수정 필요.
    @GetMapping("/{nickname}")
    public ApiSuccessResponse<List<FeedResponse>> getFeedList(
        @AuthenticationPrincipal Long userId,
        @RequestParam int page, @RequestParam int pageSize
    ){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        return ApiSuccessResponse.from(feedService.getFeedResponseList(userId, pageRequest));
    }

    // JWT와 연계할 경우, userId를 가져올 수 있다. JWT와의 연계가 끝난 후 요청 url과 파라미터를 수정한다.
    @GetMapping("/{nickname}/{feedId}")
    public ApiSuccessResponse<FeedResponse> getOneFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        return ApiSuccessResponse.from(feedService.getOneFeed(userId, feedId));
    }

    @DeleteMapping("/{feedId}")
    public ApiSuccessResponse<?> deleteFeed(
        @AuthenticationPrincipal Long userId, @PathVariable Long feedId
    ){
        feedService.deleteFeed(userId, feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
