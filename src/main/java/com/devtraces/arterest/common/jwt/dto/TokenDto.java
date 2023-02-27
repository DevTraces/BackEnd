package com.devtraces.arterest.common.jwt.dto;

import javax.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseCookie;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

	private ResponseCookie accessTokenCookie;
	private ResponseCookie refreshTokenCookie;

}
