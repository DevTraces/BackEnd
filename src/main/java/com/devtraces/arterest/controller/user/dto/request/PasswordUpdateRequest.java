package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordUpdateRequest {

    private String beforePassword;
    private String afterPassword;
}
