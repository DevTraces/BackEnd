package com.devtraces.arterest.controller.user.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProfileImageResponse {

    private String profileImageUrl;

    public static UpdateProfileImageResponse from(String profileImageUrl) {
        return UpdateProfileImageResponse.builder()
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
