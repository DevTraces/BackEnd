package com.devtraces.arterest.controller.user.dto;

import com.devtraces.arterest.common.UserSignUpType;
import com.devtraces.arterest.common.UserStatusType;
import com.devtraces.arterest.domain.user.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegistrationRequest {

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	@NotBlank(message = "비밀번호 입력은 필수입니다.")
	private String password;

	@NotBlank(message = "username 입력은 필수입니다.")
	private String username;

	@NotBlank(message = "nickname 입력은 필수입니다.")
	private String nickname;

	private MultipartFile profileImage;
	private String profileImageLink;

	private String description;

	public void setProfileImageLink(String profileImageLink) {
		this.profileImageLink = profileImageLink;
	}

	public User toEntity(String encodingPassword) {
		return User.builder()
			.email(email)
			.password(encodingPassword)
			.username(username)
			.nickname(nickname)
			.profileImageUrl(profileImageLink)
			.description(description)
			.signupType(UserSignUpType.EMAIL)
			.userStatus(UserStatusType.ACTIVE)
			.build();
	}
}
