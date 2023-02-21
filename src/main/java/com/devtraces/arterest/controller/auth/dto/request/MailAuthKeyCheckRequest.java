package com.devtraces.arterest.controller.auth.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailAuthKeyCheckRequest {

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	@NotBlank(message = "인증 코드 입력은 필수입니다.")
	private String authKey;

}
