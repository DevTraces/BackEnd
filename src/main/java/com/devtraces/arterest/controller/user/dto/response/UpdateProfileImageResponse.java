package com.devtraces.arterest.controller.user.dto.response;

import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProfileImageResponse {

    private String profileImageUrl;

    public static UpdateProfileImageResponse from(User user) {
        return UpdateProfileImageResponse.builder()
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
