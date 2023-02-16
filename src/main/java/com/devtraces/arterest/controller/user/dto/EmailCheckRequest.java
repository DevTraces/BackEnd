package com.devtraces.arterest.controller.user.dto;

import lombok.*;

import javax.validation.constraints.Email;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailCheckRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}
