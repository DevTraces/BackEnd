package com.devtraces.arterest.controller.user.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameCheckResponse {

    private boolean duplicatedNickname;

    public static NicknameCheckResponse from(boolean isDuplicated) {
        return NicknameCheckResponse.builder()
                .duplicatedNickname(isDuplicated)
                .build();
    }
}
