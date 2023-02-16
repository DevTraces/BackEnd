package com.devtraces.arterest.common.redis.service;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisService {
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private static final String ACCESS_TOKEN_BLACK_LIST_PREFIX = "AT-BL:";
	private static final String AUTH_KEY_PREFIX = "AK:";
	private static final String AUTH_COMPLETED_PREFIX = "AC:";

	private static final int AUTH_KEY_VALID_MINUTE = 10;

	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder passwordEncoder;

	public void setRefreshTokenValue(long userId, String refreshToken, Date expiredDate) {
		Date now = new Date();
		long expirationSeconds = (expiredDate.getTime() - now.getTime()) / 1000;

		if (expirationSeconds > 0) {
			ValueOperations<String, String> values = redisTemplate.opsForValue();
			values.set(
				REFRESH_TOKEN_PREFIX + userId,
				passwordEncoder.encode(refreshToken),
				Duration.ofDays(expirationSeconds));
		}
	}

	public boolean hasSameRefreshToken(long userId, String refreshToken) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		String encodingRefreshToken = values.get(REFRESH_TOKEN_PREFIX + userId);
		return passwordEncoder.matches(refreshToken, encodingRefreshToken);
	}

	public void deleteRefreshTokenBy(long userId) {
		redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
	}

	// 블랙리스트에 해당 토큰이 등록되었는지 확인하기 위해 accessToken을 key로 지정
	public void setAccessTokenBlackListValue(String accessToken, long userId, Date expiredDate) {
		Date now = new Date();
		long expirationSeconds = (expiredDate.getTime() - now.getTime()) / 1000;

		if (expirationSeconds > 0) {
			ValueOperations<String, String> values = redisTemplate.opsForValue();
			values.set(
				ACCESS_TOKEN_BLACK_LIST_PREFIX + accessToken,
				String.valueOf(userId),
				Duration.ofSeconds(expirationSeconds));
		}
	}

	public boolean notExistsInAccessTokenBlackListBy(String accessToken) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(ACCESS_TOKEN_BLACK_LIST_PREFIX + accessToken) == null;
	}

	public void setAuthKeyValue(String email, String authKey) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		values.set(AUTH_KEY_PREFIX + email, authKey, Duration.ofMinutes(AUTH_KEY_VALID_MINUTE));
	}

	public String getAuthKeyValue(String email) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(AUTH_KEY_PREFIX + email);
	}

	public void deleteAuthKeyValue(String email) {
		redisTemplate.delete(AUTH_KEY_PREFIX + email);
	}

	public void setAuthCompletedValue(String email) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		values.set(AUTH_COMPLETED_PREFIX + email, "O");
	}

	public boolean existsAuthCompletedValue(String email) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(AUTH_COMPLETED_PREFIX + email) != null;
	}

	public void deleteAuthCompletedValue(String email) {
		redisTemplate.delete(AUTH_COMPLETED_PREFIX + email);
	}
}
