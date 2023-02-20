package com.devtraces.arterest.controller.feed.bookmark;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.feed.bookmark.dto.GetBookmarkListResponse;
import com.devtraces.arterest.service.bookmark.BookmarkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark")
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@GetMapping
	public ApiSuccessResponse<List<GetBookmarkListResponse>> getBookmarkList(
		@AuthenticationPrincipal Long userId,
		@RequestParam Integer page,
		@RequestParam(required = false, defaultValue = "10") Integer pageSize
	){
		List<GetBookmarkListResponse> response =
			bookmarkService.getBookmarkList(userId, page, pageSize);
		return ApiSuccessResponse.from(response);
	}

	@PostMapping("/{feedId}")
	public ApiSuccessResponse<?> createBookmark(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long feedId
	){
		bookmarkService.createBookmark(userId, feedId);
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}

	@DeleteMapping("/{feedId}")
	public ApiSuccessResponse<?> deleteBookmark(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long feedId
	){
		bookmarkService.deleteBookmark(userId, feedId);
		return ApiSuccessResponse.NO_DATA_RESPONSE;
	}
}
