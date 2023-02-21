package com.devtraces.arterest.controller.follow;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.follow.dto.response.FollowResponse;
import com.devtraces.arterest.service.follow.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{nickname}")
    public ApiSuccessResponse<?> createFollowRelation(
        @AuthenticationPrincipal Long userId, @PathVariable String nickname
    ){
        followService.createFollowRelation(userId, nickname);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

    @GetMapping("/following/{nickname}")
    public ApiSuccessResponse<List<FollowResponse>> getFollowingUserList(
        @AuthenticationPrincipal Long userId, @PathVariable String nickname,
        @RequestParam Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        return ApiSuccessResponse.from(
            followService.getFollowingUserList(userId, nickname, page, pageSize)
        );
    }

    @GetMapping("/follower/{nickname}")
    public ApiSuccessResponse<List<FollowResponse>> getFollowerUserList(
        @AuthenticationPrincipal Long userId, @PathVariable String nickname,
        @RequestParam Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return ApiSuccessResponse.from(
            followService.getFollowerUserList(userId, nickname, page, pageSize)
        );
    }

    @DeleteMapping("/{nickname}")
    public ApiSuccessResponse<?> deleteFollowRelation(
        @AuthenticationPrincipal Long userId, @PathVariable String nickname
    ){
        followService.deleteFollowRelation(userId, nickname);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
