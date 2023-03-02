package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckAuthkeyAndSaveNewPasswordRequest {

    @Email
    private String email;

    @NotNull
    private String authKey;

    @NotNull
    private String newPassword;
}
