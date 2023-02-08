package com.devtraces.arterest.common.jwt.controller;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

import com.devtraces.arterest.common.jwt.dto.ReIssueRequest;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.jwt.service.JwtService;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class JwtController {

	private final JwtService jwtService;

	@PostMapping("/reissue")
	public ResponseEntity<ApiSuccessResponse<?>> reissue(@RequestBody @Valid ReIssueRequest request) {
		TokenDto tokenDto = jwtService.reissue(
			request.getNickname(),
			request.getAccessToken(),
			request.getRefreshToken());

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(AUTHORIZATION_HEADER,
			TOKEN_PREFIX + " " + tokenDto.getAccessToken());
		httpHeaders.add("X-REFRESH-TOKEN", tokenDto.getRefreshToken());

		return ResponseEntity
			.ok()
			.headers(httpHeaders)
			.body(ApiSuccessResponse.NO_DATA_RESPONSE);
	}

}
