package com.devtraces.arterest.controller.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordUpdateRequest {

    private String beforePassword;
    private String afterPassword;
}
