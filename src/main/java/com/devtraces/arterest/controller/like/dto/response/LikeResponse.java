package com.devtraces.arterest.controller.like.dto.response;

import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.user.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeResponse {

    private String profileImageUrl;
    private String userName;
    private String nickname;
    private boolean isFollowing;

    public static LikeResponse from(User user){
        return LikeResponse.builder()
            .profileImageUrl(user.getProfileImageUrl())
            .userName(user.getUsername())
            .nickname(user.getNickname())
            .build();
    }

}
