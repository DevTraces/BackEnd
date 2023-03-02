package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;

import javax.validation.constraints.Email;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendMailWithAuthkeyForNewPasswordRequest {

    @Email
    private String email;
}
