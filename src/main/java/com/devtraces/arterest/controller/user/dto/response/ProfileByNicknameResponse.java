package com.devtraces.arterest.controller.user.dto.response;

import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileByNicknameResponse {

    private String username;
    private String nickname;
    private String description;
    private String profileImageUrl;
    private Integer totalFeedNumber;
    private Integer followerNumber;
    private Integer followingNumber;
    private Boolean isFollowing;


    public static ProfileByNicknameResponse from(
            User user, Integer totalFeedNumber,
            Integer followerNumber, Integer followingNumber,
            boolean isFollowing
    ) {
        return ProfileByNicknameResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .description(user.getDescription())
                .profileImageUrl(user.getProfileImageUrl())
                .totalFeedNumber(totalFeedNumber)
                .followerNumber(followerNumber)
                .followingNumber(followingNumber)
                .isFollowing(isFollowing)
                .build();
    }
}