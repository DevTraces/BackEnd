package com.devtraces.arterest.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.domain.hashtag.Hashtag;
import com.devtraces.arterest.domain.hashtag.HashtagRepository;
import java.util.Arrays;
import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
	@InjectMocks
	private SearchService searchService;
	@Mock
	private HashtagRepository hashtagRepository;
	@Mock
	private Trie trie;
	@Mock
	private RedisService redisService;

	@Test
	void testInternalServerErrorInCreateAutoCompleteWords() throws Exception{

		given(hashtagRepository.findAll())
			.willReturn(Arrays.asList(Hashtag.builder()
				.hashtagString("hashtag")
				.build()));

		given(trie.put(any(),any())).willReturn(null);

		// when
		BaseException exception = assertThrows(BaseException.class,
			() -> searchService.createAutoCompleteWords());

		// then
		assertEquals(BaseException.INTERNAL_SERVER_ERROR.getErrorCode(), exception.getErrorCode());
	}

	@Test
	void testInternalServerErrorInGetAutoCompleteHashtags() throws Exception{
		// given
		given(redisService.getTrieValue(anyString()))
			.willReturn("serializedTrie");

		// when
		BaseException exception = assertThrows(BaseException.class,
			() -> searchService.getAutoCompleteHashtags("keyword", 5));

		// then
		assertEquals(BaseException.INTERNAL_SERVER_ERROR.getErrorCode(), exception.getErrorCode());
	}
}