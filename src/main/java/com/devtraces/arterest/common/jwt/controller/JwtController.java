package com.devtraces.arterest.common.jwt.controller;

import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;
import static com.devtraces.arterest.controller.auth.AuthController.ACCESS_TOKEN_PREFIX;
import static com.devtraces.arterest.controller.auth.AuthController.SET_COOKIE;

import com.devtraces.arterest.common.jwt.service.JwtService;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import java.util.HashMap;

import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import javax.servlet.http.HttpServletRequest;
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
		@RequestHeader("authorization") String bearerToken,
		@CookieValue("refreshToken") String refreshToken,
		HttpServletResponse response
	) {
		TokenWithNicknameDto dto = jwtService.reissue(bearerToken, refreshToken);

		HashMap hashMap = new HashMap();
		hashMap.put(ACCESS_TOKEN_PREFIX, TOKEN_PREFIX + " " + dto.getAccessToken());
		hashMap.put("nickname", dto.getNickname());

		response.setHeader(HttpHeaders.SET_COOKIE, dto.getCookie().toString());

		return ResponseEntity.ok()
			.body(ApiSuccessResponse.from(hashMap));
	}
}
