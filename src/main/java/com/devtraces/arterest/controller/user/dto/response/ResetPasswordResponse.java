package com.devtraces.arterest.controller.user.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResetPasswordResponse {

    private boolean isIsPasswordResetKeyCorrect;

    public static ResetPasswordResponse from(boolean isPasswordResetKeyCorrect) {
        return ResetPasswordResponse.builder()
                .isIsPasswordResetKeyCorrect(isPasswordResetKeyCorrect)
                .build();
    }
}
