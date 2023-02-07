package com.devtraces.arterest.controller;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.domain.bookmark.Bookmark;
import com.devtraces.arterest.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark")
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@PostMapping("/{feedId}")
	public ApiSuccessResponse<?> createBookmark(
		@AuthenticationPrincipal UserDetails user,
		@PathVariable Long feedId
	){
		bookmarkService.createBookmark(user.getUsername(), feedId);
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}

	@DeleteMapping("/{feedId}")
	public ApiSuccessResponse<?> deleteBookmark(
		@AuthenticationPrincipal UserDetails user,
		@PathVariable Long feedId
	){
		bookmarkService.deleteBookmark(user.getUsername(), feedId);
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}
}
