package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.dto.reply.ReplyRequest;
import com.devtraces.arterest.dto.reply.ReplyResponse;
import com.devtraces.arterest.service.ReplyService;
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
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{feedId}")
    public ApiSuccessResponse<ReplyResponse> createReply(
        @PathVariable Long feedId, @RequestBody ReplyRequest replyRequest){
        return null;
    }

    @GetMapping("/{feedId}/replies")
    public ApiSuccessResponse<List<ReplyResponse>> getReplyList(
        @AuthenticationPrincipal Long userId,
        @RequestParam Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        return null;
    }

    @PutMapping("/{feedId}/replies/{replyId}")
    public ApiSuccessResponse<ReplyResponse> updateReply(
        @AuthenticationPrincipal Long userId,
        @RequestBody ReplyRequest replyRequest
    ){
        return null;
    }

    @DeleteMapping("/{feedId}/replies/{replyId}")
    public ApiSuccessResponse<?> deleteReply(
        @AuthenticationPrincipal Long userId,
        @RequestParam Long feedId,
        @RequestParam Long replyId
    ){
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }

}
