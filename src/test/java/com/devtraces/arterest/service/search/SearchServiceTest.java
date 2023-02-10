package com.devtraces.arterest.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.domain.feed.FeedHashtagsInterface;
import com.devtraces.arterest.domain.feed.FeedRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	private FeedRepository feedRepository;
	@Mock
	private Trie trie;
	@Mock
	private RedisService redisService;

	@Test
	void testInternalServerErrorInCreateAutoCompleteWords() throws Exception{

		// given
		FeedHashtagsInterface feedHashtagsInterface = new FeedHashtagsInterface() {
			@Override
			public String getHashtags() {
				return "hashtags";
			}
		};

		List<FeedHashtagsInterface> feedHashtagsInterfaceList
			= new ArrayList<>(Arrays.asList(feedHashtagsInterface));

		given(feedRepository.findAllFeedHashtags())
			.willReturn(feedHashtagsInterfaceList);
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