package com.devtraces.arterest.common.jwt.controller;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;
import static com.devtraces.arterest.controller.user.AuthController.ACCESS_TOKEN_PREFIX;
import static com.devtraces.arterest.controller.user.AuthController.SET_COOKIE;

import com.devtraces.arterest.common.jwt.dto.ReissueRequest;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.jwt.service.JwtService;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import java.util.HashMap;
import javax.validation.Valid;

import com.devtraces.arterest.service.user.dto.TokenWithNicknameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class JwtController {

	private final JwtService jwtService;

	@PostMapping("/reissue")
	public ResponseEntity<ApiSuccessResponse<?>> reissue(
		@RequestHeader("accessToken") String accessToken,
		@CookieValue("refreshToken") String refreshToken) {
		TokenWithNicknameDto dto = jwtService.reissue(accessToken, refreshToken);

		HashMap hashMap = new HashMap();
		hashMap.put(ACCESS_TOKEN_PREFIX, TOKEN_PREFIX + " " + dto.getAccessToken());
		hashMap.put("nickname", dto.getNickname());

		return ResponseEntity.ok()
			.header(SET_COOKIE, dto.getResponseCookie().toString())
			.body(ApiSuccessResponse.from(hashMap));
	}
}
