package com.devtraces.arterest.controller.user.dto.response;

import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProfileResponse {

    private String username;
    private String nickname;
    private String description;
    private String profileImageUrl;


    public static UpdateProfileResponse from(User user) {
        return UpdateProfileResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .description(user.getDescription())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
