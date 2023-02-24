package com.devtraces.arterest.controller.auth;

import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.auth.dto.request.MailAuthKeyCheckRequest;
import com.devtraces.arterest.controller.auth.dto.response.MailAuthKeyCheckResponse;
import com.devtraces.arterest.controller.auth.dto.request.MailAuthKeyRequest;
import com.devtraces.arterest.controller.user.dto.request.PasswordCheckRequest;
import com.devtraces.arterest.controller.user.dto.response.PasswordCheckResponse;
import com.devtraces.arterest.controller.auth.dto.request.SignInRequest;
import com.devtraces.arterest.controller.auth.dto.request.UserRegistrationRequest;
import com.devtraces.arterest.controller.auth.dto.response.UserRegistrationResponse;
import com.devtraces.arterest.service.auth.AuthService;
import java.util.HashMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.annotation.Nullable;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	public static final String SET_COOKIE = "Cookie";
	public static final String ACCESS_TOKEN_PREFIX = "accessToken";

	@PostMapping("sign-up")
	public ApiSuccessResponse<UserRegistrationResponse> signUp(
			@RequestParam @Email(message = "이메일 형식이 올바르지 않습니다.") String email,
			@RequestParam @NotBlank(message = "비밀번호 입력은 필수입니다.") String password,
			@RequestParam @NotBlank(message = "username 입력은 필수입니다.") String username,
			@RequestParam @NotBlank(message = "nickname 입력은 필수입니다.") String nickname,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
			@RequestParam @Nullable String description
			) {
		UserRegistrationResponse response = authService.register(
				email, password,
				username, nickname,
				profileImage, description
		);
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
	public ResponseEntity<ApiSuccessResponse<?>> signIn(
		@RequestBody @Valid SignInRequest request,
		HttpServletResponse response
	) {
		TokenWithNicknameDto dto = authService.signInAndGenerateJwtToken(
				request.getEmail(),
				request.getPassword()
		);

		HashMap hashMap = new HashMap();
		hashMap.put(ACCESS_TOKEN_PREFIX, TOKEN_PREFIX + " " + dto.getAccessToken());
		hashMap.put("nickname", dto.getNickname());

		response.setHeader(HttpHeaders.SET_COOKIE, dto.getCookie().toString());

		return ResponseEntity.ok()
				.body(ApiSuccessResponse.from(hashMap));
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
