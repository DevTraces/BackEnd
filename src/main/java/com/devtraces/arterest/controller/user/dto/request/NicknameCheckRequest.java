package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameCheckRequest {

    @NotNull(message = "닉네임 입력은 필수입니다.")
    private String nickname;
}
