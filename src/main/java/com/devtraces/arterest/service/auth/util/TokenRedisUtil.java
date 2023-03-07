package com.devtraces.arterest.service.auth.util;

import static com.devtraces.arterest.common.jwt.JwtProvider.REFRESH_TOKEN_SUBJECT_PREFIX;

import com.devtraces.arterest.common.exception.BaseException;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenRedisUtil {
	private static final String ACCESS_TOKEN_BLACK_LIST_PREFIX = "AT-BL:";

	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder passwordEncoder;

	public void setRefreshTokenValue(long userId, String refreshToken, Date expiredDate) {
		Date now = new Date();
		long expirationSeconds = (expiredDate.getTime() - now.getTime()) / 1000;

		if (expirationSeconds > 0) {
			ValueOperations<String, String> values = redisTemplate.opsForValue();
			values.set(
				REFRESH_TOKEN_SUBJECT_PREFIX + userId,
				passwordEncoder.encode(refreshToken),
				Duration.ofDays(expirationSeconds));
		}
	}

	public boolean hasSameRefreshToken(long userId, String refreshToken) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		String encodingRefreshToken = values.get(REFRESH_TOKEN_SUBJECT_PREFIX + userId);
		if(encodingRefreshToken == null) throw BaseException.INVALID_TOKEN;

		return passwordEncoder.matches(refreshToken, encodingRefreshToken);
	}

	public void deleteRefreshTokenBy(long userId) {
		redisTemplate.delete(REFRESH_TOKEN_SUBJECT_PREFIX + userId);
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
}
