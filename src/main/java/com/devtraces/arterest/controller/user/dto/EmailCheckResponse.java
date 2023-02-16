package com.devtraces.arterest.controller.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailCheckResponse {

    private boolean duplicatedEmail;

    public static EmailCheckResponse from(boolean isDuplicated) {
        return EmailCheckResponse.builder()
                .duplicatedEmail(isDuplicated)
                .build();
    }
}
