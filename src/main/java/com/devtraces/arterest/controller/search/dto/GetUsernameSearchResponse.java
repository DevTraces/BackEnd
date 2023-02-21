package com.devtraces.arterest.controller.search.dto;

import com.devtraces.arterest.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GetUsernameSearchResponse {
	private Long userId;
	private String username;
	private String nickname;
	private String profileImageUrl;

	public static GetUsernameSearchResponse from(User user) {
		return GetUsernameSearchResponse.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImageUrl())
			.build();
	}
}
