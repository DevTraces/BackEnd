package com.devtraces.arterest.controller.user.dto;

import com.devtraces.arterest.domain.user.User;
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


    public static ProfileByNicknameResponse from(User user, Integer totalFeedNumber) {
        return ProfileByNicknameResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .description(user.getDescription())
                .profileImageUrl(user.getProfileImageUrl())
                .totalFeedNumber(totalFeedNumber)
                .followerNumber(0) // TODO : follow 로직 완료시 추가될 예정
                .followingNumber(0) // TODO : follow 로직 완료시 추가될 예정
                .isFollowing(false) // TODO : follow 로직 완료시 추가될 예정
                .build();
    }
}