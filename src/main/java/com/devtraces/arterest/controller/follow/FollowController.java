package com.devtraces.arterest.controller.follow;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{nickname}")
    public ApiSuccessResponse<?> createFollowRelation(
        @AuthenticationPrincipal Long userId,
        @PathVariable String nickname
    ){
        followService.createFollowRelation(userId, nickname);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
