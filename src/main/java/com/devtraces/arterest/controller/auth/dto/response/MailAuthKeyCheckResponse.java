package com.devtraces.arterest.controller.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailAuthKeyCheckResponse {

	private String signUpKey;
	private boolean isIsCorrect;

	public static MailAuthKeyCheckResponse from(String signUpKey, boolean isIsCorrect) {
		return MailAuthKeyCheckResponse.builder()
				.signUpKey(signUpKey)
				.isIsCorrect(isIsCorrect)
				.build();
	}
}
