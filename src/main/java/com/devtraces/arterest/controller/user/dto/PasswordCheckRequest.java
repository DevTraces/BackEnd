package com.devtraces.arterest.controller.user.dto;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordCheckRequest {

	@NotBlank(message = "비밀번호 입력은 필수입니다.")
	private String password;

}
