package com.devtraces.arterest.common.redis.service;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisService {
	private static final String REFRESH_TOKEN_PREFIX = "RT:";

	private final RedisTemplate<String, String> redisTemplate;

	public void setEncodingRefreshTokenValue(long userId, String encodingRefreshToken, Date expiredDate) {
		Date now = new Date();
		long expirationSeconds = (expiredDate.getTime() - now.getTime()) / 1000;

		if (expirationSeconds > 0) {
			ValueOperations<String, String> values = redisTemplate.opsForValue();
			values.set(
				REFRESH_TOKEN_PREFIX + userId,
				encodingRefreshToken,
				Duration.ofDays(expirationSeconds));
		}
	}

	public String getEncodingRefreshTokenValue(long userId) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(REFRESH_TOKEN_PREFIX + userId);
	}
}
