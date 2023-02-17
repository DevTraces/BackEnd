package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.component.S3Uploader;
import com.devtraces.arterest.common.component.MailUtil;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;

import com.devtraces.arterest.controller.user.dto.UserRegistrationRequest;
import com.devtraces.arterest.controller.user.dto.UserRegistrationResponse;

import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;

import java.util.Date;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

	private static final int AUTH_KEY_DIGIT = 6;

	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final S3Uploader s3Uploader;
	private final MailUtil mailUtil;
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
		if (redisService.notExistsAuthCompletedValue(request.getEmail())) {
			throw BaseException.NOT_AUTHENTICATION_YET;
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		if (userRepository.existsByNickname(request.getNickname())) {
			throw BaseException.ALREADY_EXIST_NICKNAME;
		}
	}

	@Transactional(readOnly = true)
	public void sendMailWithAuthKey(String email) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKey = generateAuthKey();
		sendAuthenticationEmail(email, authKey);
		redisService.setAuthKeyValue(email, authKey);
	}

	protected String generateAuthKey() {
		Random random = new Random();
		StringBuilder resultNumber = new StringBuilder();

		for (int i = 0; i < AUTH_KEY_DIGIT; i++) {
			resultNumber.append(random.nextInt(9));	// 0~9 사이의 랜덤 숫자 생성
		}
		return resultNumber.toString();
	}

	private void sendAuthenticationEmail(String email, String authKey) {
		String subject = "Arterest 인증 코드";
		String text = "<h2>이메일 인증코드</h2>\n"
			+ "<p>Arterest에 가입하신 것을 환영합니다.<br>아래의 인증코드를 입력하시면 가입이 정상적으로 완료됩니다.</p>\n"
			+ "<p style=\"background: #EFEFEF; font-size: 30px;padding: 10px\">" + authKey + "</p>";
		mailUtil.sendMail(email, subject, text);
	}

	public boolean checkAuthKey(String email, String authKey) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKeyInRedis = redisService.getAuthKeyValue(email);
		if (!authKey.equals(authKeyInRedis)) {
			return false;
		}
		// 인증 완료했으므로 Redis 정보 변경
		redisService.deleteAuthKeyValue(email);
		redisService.setAuthCompletedValue(email);
		return true;
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

	public boolean checkPassword(long userId, String password) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);
		return passwordEncoder.matches(password, user.getPassword());
	}
}
