package com.devtraces.arterest.controller.user.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckAuthkeyAndSaveNewPasswordResponse {

    private boolean isCorrect;

    public static CheckAuthkeyAndSaveNewPasswordResponse from(boolean isCorrect
    ) {
        return CheckAuthkeyAndSaveNewPasswordResponse.builder()
                .isCorrect(isCorrect)
                .build();
    }
}
