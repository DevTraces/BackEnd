package com.devtraces.arterest.service.auth.util;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthRedisUtil {
	private static final String AUTH_KEY_PREFIX = "AK:";
	private static final String AUTH_COMPLETED_PREFIX = "AC:";

	private static final int AUTH_KEY_VALID_MINUTE = 10;

	private final RedisTemplate<String, String> redisTemplate;

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

	public boolean notExistsAuthCompletedValue(String email) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(AUTH_COMPLETED_PREFIX + email) == null;
	}

	public void deleteAuthCompletedValue(String email) {
		redisTemplate.delete(AUTH_COMPLETED_PREFIX + email);

	}
}
