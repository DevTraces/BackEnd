package com.devtraces.arterest.controller.search;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.search.dto.GetHashtagsSearchResponse;
import com.devtraces.arterest.controller.search.dto.GetNicknameSearchResponse;
import com.devtraces.arterest.controller.search.dto.GetUsernameSearchResponse;
import com.devtraces.arterest.service.search.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
	private final SearchService searchService;

	@GetMapping("/autocomplete")
	public ApiSuccessResponse<List<String>> getAutoCompleteWords(
		@RequestParam String keyword,
		@RequestParam(required = false, defaultValue = "5") Integer numberOfWords
	){
		List<String> response =
			searchService.getAutoCompleteWords(keyword, numberOfWords);
		return ApiSuccessResponse.from(response);
	}

	@GetMapping("/hashtags")
	public ApiSuccessResponse<GetHashtagsSearchResponse> getSearchResultUsingHashtags(
		@RequestParam String keyword,
		@RequestParam Integer page,
		@RequestParam(required = false, defaultValue = "10") Integer pageSize
	){
		GetHashtagsSearchResponse response =
			searchService.getSearchResultUsingHashtags(keyword, page, pageSize);
		return ApiSuccessResponse.from(response);
	}

	@GetMapping("/username")
	public ApiSuccessResponse<List<GetUsernameSearchResponse>> getSearchResultUsingUsername(
		@RequestParam String keyword,
		@RequestParam Integer page,
		@RequestParam(required = false, defaultValue = "10") Integer pageSize
	){
		List<GetUsernameSearchResponse> response =
			searchService.getSearchResultUsingUsername(keyword, page, pageSize);
		return ApiSuccessResponse.from(response);
	}

	@GetMapping("/nickname")
	public ApiSuccessResponse<List<GetNicknameSearchResponse>> getSearchResultUsingNickname(
		@RequestParam String keyword,
		@RequestParam Integer page,
		@RequestParam(required = false, defaultValue = "10") Integer pageSize
	){
		List<GetNicknameSearchResponse> response =
			searchService.getSearchResultUsingNickname(keyword, page, pageSize);
		return ApiSuccessResponse.from(response);
	}
}
