package com.devtraces.arterest.controller.user;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.SignInRequest;
import com.devtraces.arterest.service.user.AuthService;
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
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/sign-in")
	public ResponseEntity<ApiSuccessResponse<?>> signIn(@RequestBody @Valid SignInRequest request) {
		TokenDto tokenDto = authService.signInAndGenerateJwtToken(request.getEmail(),
			request.getPassword());

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
