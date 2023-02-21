package com.devtraces.arterest.controller.user.dto;

import com.devtraces.arterest.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegistrationResponse {

	private String email;
	private String username;
	private String nickname;
	private String profileImageUrl;
	private String description;

	public static UserRegistrationResponse from(User user) {
		return UserRegistrationResponse.builder()
			.email(user.getEmail())
			.username(user.getUsername())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImageUrl())
			.description(user.getDescription())
			.build();
	}
}
