package com.devtraces.arterest.controller.like;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.like.dto.response.LikeResponse;
import com.devtraces.arterest.service.like.LikeService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like/{feedId}")
    public ApiSuccessResponse<?> createLike(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long feedId
    ){
        likeService.pressLikeOnFeed(userId, feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

    @DeleteMapping("/like/{feedId}")
    public ApiSuccessResponse<?> deleteLike(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long feedId
    ){
        likeService.cancelLikeOnFeed(userId, feedId);
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

    @GetMapping("/feeds/like/{feedId}")
    public ApiSuccessResponse<List<LikeResponse>> getLikedUserList(
        @PathVariable Long feedId,
        @RequestParam int page,
        @RequestParam(required = false, defaultValue = "10") int pageSize
    ){
        return ApiSuccessResponse.from(
            likeService.getLikedUserList(feedId, page, pageSize)
        );
    }

}
