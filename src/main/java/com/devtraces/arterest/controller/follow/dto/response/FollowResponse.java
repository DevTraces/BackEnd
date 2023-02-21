package com.devtraces.arterest.controller.follow.dto.response;

import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.model.user.User;
import java.time.LocalDateTime;
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
public class FollowResponse {

		private Long userId;
		private String profileImageUrl;
		private String username;
		private String nickname;
		private String description;
		private boolean isFollowing;
		private LocalDateTime createdAt;
		private LocalDateTime modifiedAt;
		private UserSignUpType signupType;

		public static FollowResponse from(User user, Set<Long> followingUserIdSet){
			return FollowResponse.builder()
				.userId(user.getId())
				.profileImageUrl(user.getProfileImageUrl())
				.username(user.getUsername())
				.nickname(user.getNickname())
				.description(user.getDescription())
				.isFollowing(
					followingUserIdSet == null ? false :
					followingUserIdSet.contains(user.getId())
					)
				.build();
		}

}
