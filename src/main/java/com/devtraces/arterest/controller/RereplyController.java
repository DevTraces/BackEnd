package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.dto.rereply.RereplyRequest;
import com.devtraces.arterest.dto.rereply.RereplyResponse;
import com.devtraces.arterest.service.RereplyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class RereplyController {

    private final RereplyService rereplyService;

    @PostMapping("/{feedId}/replies/{replyId}/rereplies")
    public ApiSuccessResponse<RereplyResponse> createRereply(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long feedId, @PathVariable Long replyId,
        @RequestBody RereplyRequest rereplyRequest
    ){
        return null;
    }

    @GetMapping("/{feedId}/replies/{replyId}/rereplies")
    public ApiSuccessResponse<List<RereplyResponse>> getRereplyList(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long feedId, @PathVariable Long replyId,
        @RequestParam Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        return null;
    }

    @PutMapping("/{feedId}/replies/{replyId}/rereplies")
    public ApiSuccessResponse<RereplyResponse> updateRereply(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long feedId, @PathVariable Long replyId,
        @RequestBody RereplyRequest rereplyRequest
    ){
        return null;
    }

    @DeleteMapping("/{feedId}/replies/{replyId}/rereplies/{rereplyId}")
    public ApiSuccessResponse<?> deleteRereply(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long rereplyId
    ){
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
