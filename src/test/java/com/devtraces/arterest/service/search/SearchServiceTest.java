package com.devtraces.arterest.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.controller.search.dto.GetHashtagsSearchResponse;
import com.devtraces.arterest.controller.search.dto.GetNicknameSearchResponse;
import com.devtraces.arterest.controller.search.dto.GetUsernameSearchResponse;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedHashtagsInterface;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.hashtag.Hashtag;
import com.devtraces.arterest.domain.hashtag.HashtagRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
	@InjectMocks
	private SearchService searchService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private FeedRepository feedRepository;
	@Mock
	private HashtagRepository hashtagRepository;
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

	@Test
	void TestGetSearchResultUsingHashtags() throws Exception{
		//given
		List<Hashtag> responseList = Arrays.asList(Hashtag.builder()
			.feed(Feed.builder()
				.id(1L)
				.imageUrls("imageUrl")
				.build())
			.build());

		Pageable pageable = PageRequest.of(0, 10);

		Page<Hashtag> bookmarkPage = new PageImpl<>(responseList, pageable, 1);

		given(hashtagRepository.findByHashtag(anyString(), any()))
			.willReturn(bookmarkPage);

		//when
		List<GetHashtagsSearchResponse> response = searchService.getSearchResultUsingHashtags("keyword", 0, 10);

		//then
		assertEquals(1L, response.get(0).getFeedId());
		assertEquals("imageUrl", response.get(0).getImageUrl());
	}

	@Test
	void TestGetSearchResultUsingUsername() throws Exception{
		//given
		List<User> responseList = Arrays.asList(User.builder()
			.id(1L)
			.username("username")
			.nickname("nickname")
			.profileImageUrl("profileImageUrl")
			.build());

		Pageable pageable = PageRequest.of(0, 10);

		Page<User> userPage = new PageImpl<>(responseList, pageable, 1);

		given(userRepository.findByUsername(anyString(), any()))
			.willReturn(userPage);

		//when
		List<GetUsernameSearchResponse> response = searchService.getSearchResultUsingUsername("keyword", 0, 10);

		//then
		assertEquals(1L, response.get(0).getUserId());
		assertEquals("username", response.get(0).getUsername());
		assertEquals("nickname", response.get(0).getNickname());
		assertEquals("profileImageUrl", response.get(0).getProfileImageUrl());
	}

	@Test
	void TestGetSearchResultUsingNickname() throws Exception{
		//given
		List<User> responseList = Arrays.asList(User.builder()
			.id(1L)
			.username("username")
			.nickname("nickname")
			.profileImageUrl("profileImageUrl")
			.build());

		Pageable pageable = PageRequest.of(0, 10);

		Page<User> userPage = new PageImpl<>(responseList, pageable, 1);

		given(userRepository.findByNickname(anyString(), any()))
			.willReturn(userPage);

		//when
		List<GetNicknameSearchResponse> response = searchService.getSearchResultUsingNickname("keyword", 0, 10);

		//then
		assertEquals(1L, response.get(0).getUserId());
		assertEquals("username", response.get(0).getUsername());
		assertEquals("nickname", response.get(0).getNickname());
		assertEquals("profileImageUrl", response.get(0).getProfileImageUrl());
	}
}