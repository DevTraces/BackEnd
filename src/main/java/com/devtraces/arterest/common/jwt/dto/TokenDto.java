package com.devtraces.arterest.common.jwt.dto;

import javax.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

	private String accessToken;
	private Cookie cookie;

}
