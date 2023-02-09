package com.devtraces.arterest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.domain.bookmark.Bookmark;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.dto.GetBookmarkListResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

	@InjectMocks
	private BookmarkService bookmarkService;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Test
	void testGetBookmarkList() throws Exception{
		//given
		List<Bookmark> responseList = Arrays.asList(Bookmark.builder()
			.feed(Feed.builder()
				.id(1L)
				.imageUrls("imageUrl")
				.build())
			.build());

		Pageable pageable = PageRequest.of(0, 10);

		Page<Bookmark> bookmarkPage = new PageImpl<>(responseList, pageable, 1);

		given(bookmarkRepository.findByUserId(anyLong(), any()))
			.willReturn(bookmarkPage);

		//when
		List<GetBookmarkListResponse> response = bookmarkService.getBookmarkList(1L, 0, 10);

		//then
		assertEquals(1L, response.get(0).getFeedId());
		assertEquals("imageUrl", response.get(0).getImageUrl());
	}

	@Test
	void testCreateBookmark() throws Exception{
		// given
		ArgumentCaptor<Bookmark> captor = ArgumentCaptor.forClass(Bookmark.class);

		// when
		bookmarkService.createBookmark(1L, 2L);

		// then
		verify(bookmarkRepository, times(1)).save(captor.capture());
		assertEquals(1L, captor.getValue().getUser().getId());
		assertEquals(2L, captor.getValue().getFeed().getId());
	}

	@Test
	void testDeleteBookmark() throws Exception{
		// given
		willDoNothing().given(bookmarkRepository).deleteByUserIdAndFeedId(anyLong(), anyLong());

		// when
		bookmarkService.deleteBookmark(1L, 2L);

		// then
		verify(bookmarkRepository, times(1)).deleteByUserIdAndFeedId(1L, 2L);
	}
}