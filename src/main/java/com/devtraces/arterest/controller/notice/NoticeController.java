package com.devtraces.arterest.controller.notice;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.controller.notice.dto.NumberOfNoticeResponse;
import com.devtraces.arterest.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/counts")
    public ApiSuccessResponse<NumberOfNoticeResponse> getNumberOfNotices(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiSuccessResponse.from(noticeService.getNumberOfNotice(userId));
    }

    @GetMapping()
    public ApiSuccessResponse<List<NoticeListResponse>> getNoticeList(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiSuccessResponse.from(noticeService.getNoticeList(userId));
    }
}