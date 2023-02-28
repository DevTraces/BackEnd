package com.devtraces.arterest.controller.notice.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class NoticeListResponse {

    // 말줄임표
    public static final String ELLIPSIS = "...";

    public static final int MAX_CONTENT_LENGTH = 125;

    protected static String getShortenContent(String content) {
        return content.length() > MAX_CONTENT_LENGTH ?
                content.substring(0, MAX_CONTENT_LENGTH) + ELLIPSIS : content;
    }
}
