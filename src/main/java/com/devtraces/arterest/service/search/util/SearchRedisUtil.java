package com.devtraces.arterest.service.search.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SearchRedisUtil {

	private final RedisTemplate<String, String> redisTemplate;


	public void setTrieValue(String key, String data) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		values.set(key, data);
	}

	public String getTrieValue(String key) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(key);
	}
}
