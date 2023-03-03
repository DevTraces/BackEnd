package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckAuthkeyAndSaveNewPasswordRequest {

    @NotNull
    private String authKey;
}
