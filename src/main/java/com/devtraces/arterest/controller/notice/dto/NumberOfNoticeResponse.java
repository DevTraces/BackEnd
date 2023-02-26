package com.devtraces.arterest.controller.notice.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NumberOfNoticeResponse {

    private Integer noticeNumber;

    public static NumberOfNoticeResponse from(Integer noticeNumber) {
        return NumberOfNoticeResponse.builder()
                .noticeNumber(noticeNumber)
                .build();
    }
}
