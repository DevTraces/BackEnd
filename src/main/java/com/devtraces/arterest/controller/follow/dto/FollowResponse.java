package com.devtraces.arterest.controller.follow.dto;

import com.devtraces.arterest.common.UserSignUpType;
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

}
