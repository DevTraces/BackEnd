package com.devtraces.arterest.common.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseCookie;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

	private String accessToken;
	private ResponseCookie responseCookie;

}
