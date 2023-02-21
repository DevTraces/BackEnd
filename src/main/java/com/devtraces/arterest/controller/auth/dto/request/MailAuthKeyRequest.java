package com.devtraces.arterest.controller.auth.dto.request;

import javax.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailAuthKeyRequest {

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;
}
