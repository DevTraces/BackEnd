package com.devtraces.arterest.common.jwt.controller;

import static com.devtraces.arterest.common.jwt.JwtProvider.ACCESS_TOKEN_COOKIE_NAME;
import static com.devtraces.arterest.common.jwt.JwtProvider.REFRESH_TOKEN_COOKIE_NAME;

import com.devtraces.arterest.common.jwt.service.JwtService;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class JwtController {

	private final JwtService jwtService;

	@PostMapping("/reissue")
	public ResponseEntity<ApiSuccessResponse<?>> reissue(
		@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
		HttpServletResponse response
	) {
		TokenWithNicknameDto dto = jwtService.reissue(refreshToken);

		HashMap hashMap = new HashMap();
		hashMap.put("nickname", dto.getNickname());

		response.addHeader(HttpHeaders.SET_COOKIE, dto.getAcceesTokenCookie().toString());
		response.addHeader(HttpHeaders.SET_COOKIE, dto.getRefreshTokenCookie().toString());

		return ResponseEntity.ok()
			.body(ApiSuccessResponse.from(hashMap));
	}
}
