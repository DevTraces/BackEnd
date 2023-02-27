package com.devtraces.arterest.service.search.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SearchRedisService {

	private final RedisTemplate<String, String> redisTemplate;
	private final String TRIE_KEY = "trie";


	public void setTrieValue(String data) {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		values.set(TRIE_KEY, data);
	}

	public String getTrieValue() {
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get(TRIE_KEY);
	}
}
