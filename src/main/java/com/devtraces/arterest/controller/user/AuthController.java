package com.devtraces.arterest.controller.user;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.MailAuthKeyCheckRequest;
import com.devtraces.arterest.controller.user.dto.MailAuthKeyCheckResponse;
import com.devtraces.arterest.controller.user.dto.MailAuthKeyRequest;
import com.devtraces.arterest.controller.user.dto.PasswordCheckRequest;
import com.devtraces.arterest.controller.user.dto.PasswordCheckResponse;
import com.devtraces.arterest.controller.user.dto.SignInRequest;
import com.devtraces.arterest.controller.user.dto.UserRegistrationRequest;
import com.devtraces.arterest.controller.user.dto.UserRegistrationResponse;
import com.devtraces.arterest.service.user.AuthService;
import java.util.HashMap;
import javax.servlet.http.Cookie;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	public static final String SET_COOKIE = "Set-Cookie";
	public static final String ACCESS_TOKEN_PREFIX = "accessToken";

	@PostMapping("sign-up")
	public ApiSuccessResponse<UserRegistrationResponse> signUp(@ModelAttribute @Valid UserRegistrationRequest request) {
		UserRegistrationResponse response = authService.register(request);
		return ApiSuccessResponse.from(response);
	}

	@PostMapping("/email/auth-key")
	public ApiSuccessResponse<?> sendMailWithAuthKey(@RequestBody @Valid MailAuthKeyRequest request) {
		authService.sendMailWithAuthKey(request.getEmail());
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}

	@PostMapping("/email/auth-key/check")
	public ApiSuccessResponse<MailAuthKeyCheckResponse> checkAuthKey(@RequestBody @Valid MailAuthKeyCheckRequest request) {
		boolean isCorrect = authService.checkAuthKey(request.getEmail(), request.getAuthKey());
		return ApiSuccessResponse.from(new MailAuthKeyCheckResponse(isCorrect));
	}

	@PostMapping("/sign-in")
	public ResponseEntity<ApiSuccessResponse<?>> signIn(@RequestBody @Valid SignInRequest request) {
		TokenDto tokenDto = authService.signInAndGenerateJwtToken(request.getEmail(),
			request.getPassword());

		return ResponseEntity.ok()
				.header(SET_COOKIE, tokenDto.getResponseCookie().toString())
				.body(ApiSuccessResponse.from(new HashMap(){{
					put(ACCESS_TOKEN_PREFIX, TOKEN_PREFIX + " " + tokenDto.getAccessToken());
				}}));
	}

	@PostMapping("/sign-out")
	public ApiSuccessResponse<?> signOut(@AuthenticationPrincipal long userId, @RequestHeader("authorization") String bearerToken) {
		String accessToken = bearerToken.substring(TOKEN_PREFIX.length() + 1);
		authService.signOut(userId, accessToken);
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}

	@PostMapping("/password/check")
	public ApiSuccessResponse<PasswordCheckResponse> checkPassword(@AuthenticationPrincipal long userId, @RequestBody @Valid PasswordCheckRequest request) {
		boolean isCorrect = authService.checkPassword(userId, request.getPassword());
		return ApiSuccessResponse.from(PasswordCheckResponse.builder().isCorrect(isCorrect).build());
	}
}
