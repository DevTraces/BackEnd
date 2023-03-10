package com.devtraces.arterest.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.search.dto.response.GetHashtagsSearchResponse;
import com.devtraces.arterest.controller.search.dto.response.GetNicknameSearchResponse;
import com.devtraces.arterest.controller.search.dto.response.GetUsernameSearchResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.hashtag.Hashtag;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.search.util.SearchRedisService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
	private HashtagRepository hashtagRepository;
	@Mock
	private Trie trie;
	@Mock
	private SearchRedisService searchRedisService;

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
		given(searchRedisService.getTrieValue())
			.willReturn("serializedTrie");

		// when
		BaseException exception = assertThrows(BaseException.class,
			() -> searchService.getAutoCompleteHashtags("keyword", 5));

		// then
		assertEquals(BaseException.INTERNAL_SERVER_ERROR.getErrorCode(), exception.getErrorCode());
	}

	@Test
	void TestGetSearchResultUsingHashtags(){
		//given
		List<FeedHashtagMap> feedHashtagMapList = new ArrayList<>(
			Arrays.asList(
			FeedHashtagMap.builder()
				.feed(Feed.builder()
					.id(1L)
					.imageUrls("imageUrl1,imageUrl2")
					.build())
				.hashtag(Hashtag.builder()
					.id(11L)
					.hashtagString("keyword")
					.build())
				.build(),
			FeedHashtagMap.builder()
				.feed(Feed.builder()
					.id(2L)
					.imageUrls("imageUrl3,imageUrl4")
					.build())
				.hashtag(Hashtag.builder()
					.id(12L)
					.hashtagString("keyword")
					.build())
				.build()));

		Hashtag hashtag = Hashtag.builder()
			.feedHashtagMapList(feedHashtagMapList)
			.hashtagString("keyword")
			.build();

		given(hashtagRepository.findByHashtagString(anyString()))
			.willReturn(Optional.ofNullable(hashtag));

		//when
		GetHashtagsSearchResponse response = searchService.getSearchResultUsingHashtags("keyword", 0, 10);

		//then
		assertEquals(2L, response.getTotalNumberOfSearches());
		assertEquals(1L, response.getFeedList().get(0).getFeedId());
		assertEquals("imageUrl1", response.getFeedList().get(0).getImageUrl());
		assertEquals(2L, response.getFeedList().get(1).getFeedId());
		assertEquals("imageUrl3", response.getFeedList().get(1).getImageUrl());
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

		int start = (int) pageable.getOffset();
		int end = (start + pageable.getPageSize()) > responseList.size() ?
			responseList.size() : (start + pageable.getPageSize());

		Page<User> userPage = new PageImpl<>(
			responseList.subList(start, end), pageable, responseList.size());

		given(userRepository.findByUsernameStartsWith(anyString(), any()))
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

		int start = (int) pageable.getOffset();
		int end = (start + pageable.getPageSize()) > responseList.size() ?
			responseList.size() : (start + pageable.getPageSize());

		Page<User> userPage = new PageImpl<>(
			responseList.subList(start, end), pageable, responseList.size());

		given(userRepository.findByNicknameStartsWith(anyString(), any()))
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