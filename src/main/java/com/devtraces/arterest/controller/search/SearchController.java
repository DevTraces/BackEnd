package com.devtraces.arterest.controller.search;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.bookmark.dto.GetBookmarkListResponse;
import com.devtraces.arterest.service.search.SearchService;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
	private final SearchService searchService;

	@GetMapping
	public ApiSuccessResponse<List<String>> getAutoCompleteWords(
		@RequestParam String keyword,
		@RequestParam(required = false, defaultValue = "5") Integer numberOfWords
	){
		List<String> response =
			searchService.getAutoCompleteWords(keyword, numberOfWords);
		return ApiSuccessResponse.from(response);
	}
}
