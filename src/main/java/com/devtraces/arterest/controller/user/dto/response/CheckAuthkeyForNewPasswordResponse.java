package com.devtraces.arterest.controller.user.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckAuthkeyForNewPasswordResponse {

    private boolean isIsCorrect; // boolean에서 is가 생략됨
    private String passwordResetKey;

    public static CheckAuthkeyForNewPasswordResponse from(
            boolean isCorrect, String passwordResetKey
    ) {
        return CheckAuthkeyForNewPasswordResponse.builder()
                .isIsCorrect(isCorrect)
                .passwordResetKey(passwordResetKey)
                .build();
    }
}
