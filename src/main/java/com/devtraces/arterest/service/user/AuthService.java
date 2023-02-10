package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.component.S3Uploader;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.controller.user.dto.UserRegistrationRequest;
import com.devtraces.arterest.controller.user.dto.UserRegistrationResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final S3Uploader s3Uploader;
	private final RedisService redisService;
	private final UserRepository userRepository;

	@Transactional
	public UserRegistrationResponse register(UserRegistrationRequest request) {
		uploadAndUpdateImageUrl(request);
		validateRegistration(request);

		String encodingPassword = passwordEncoder.encode(request.getPassword());
		User user = request.toEntity(encodingPassword);
		User savedUser = userRepository.save(user);
		return UserRegistrationResponse.from(savedUser);
	}

	private void uploadAndUpdateImageUrl(UserRegistrationRequest request) {
		if (request.getProfileImage() != null) {
			String profileImageUrl = s3Uploader.uploadImage(request.getProfileImage());
			request.setProfileImageLink(profileImageUrl);
		}
	}

	private void validateRegistration(UserRegistrationRequest request) {
		// TODO: 이메일 인증을 하지 않은 경우

		if (userRepository.existsByEmail(request.getEmail())) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		if (userRepository.existsByNickname(request.getNickname())) {
			throw BaseException.ALREADY_EXIST_NICKNAME;
		}
	}

	@Transactional(readOnly = true)
	public TokenDto signInAndGenerateJwtToken(String email, String password) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BaseException.WRONG_EMAIL_OR_PASSWORD);
		validateLogin(user, password);

		return jwtProvider.generateAccessTokenAndRefreshToken(user.getId());
	}

	private void validateLogin(User user, String passwordInput) {
		if (!passwordEncoder.matches(passwordInput, user.getPassword())) {
			throw BaseException.WRONG_EMAIL_OR_PASSWORD;
		}
	}

	@Transactional
	public void signOut(long userId, String accessToken) {
		redisService.deleteRefreshTokenBy(userId);

		// Access Token을 무효화시킬 수 없으므로 Redis에 블랙리스트 작성
		Date expiredDate = jwtProvider.getExpiredDate(accessToken);
		redisService.setAccessTokenBlackListValue(accessToken, userId, expiredDate);
	}
}
