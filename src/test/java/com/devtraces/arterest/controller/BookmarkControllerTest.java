package com.devtraces.arterest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devtraces.arterest.dto.GetBookmarkListResponseDto;
import com.devtraces.arterest.service.BookmarkService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {
	@MockBean
	private BookmarkService bookmarkService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void testGetBookmarkList() throws Exception{
		// given
		List<GetBookmarkListResponseDto> response =
			Arrays.asList(GetBookmarkListResponseDto.builder()
				.feedId(1L)
				.imageUrl("imageUrl")
				.build());

		given(bookmarkService.getBookmarkList(any(), anyInt(), anyInt()))
			.willReturn(response);

		// when
		// then
		mockMvc.perform(get("/api/bookmark?page=0&pageSize=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].feedId").value(1L))
			.andExpect(jsonPath("$.data[0].imageUrl").value("imageUrl"));
	}

	@Test
	@WithMockUser
	void testCreateBookmark() throws Exception{

		// given
		willDoNothing()
			.given(bookmarkService).createBookmark(anyLong(), anyLong());

		// when
		// then
		mockMvc.perform(post("/api/bookmark/1")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void testDeleteBookmark() throws Exception{

		//given
		willDoNothing()
			.given(bookmarkService).deleteBookmark(anyLong(), anyLong());

		//when
		//then
		mockMvc.perform(delete("/api/bookmark/1")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isOk());
	}

}