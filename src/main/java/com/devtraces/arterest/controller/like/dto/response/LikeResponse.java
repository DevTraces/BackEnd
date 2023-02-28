package com.devtraces.arterest.controller.like.dto.response;

import com.devtraces.arterest.model.user.User;
import java.util.Set;
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
    private String username;
    private String nickname;
    private boolean isFollowing;

    public static LikeResponse from(User user, Set<Long> likedUserIdSet){
        return LikeResponse.builder()
            .profileImageUrl(user.getProfileImageUrl())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .isFollowing(
                likedUserIdSet != null && likedUserIdSet.contains(user.getId())
            )
            .build();
    }

}
