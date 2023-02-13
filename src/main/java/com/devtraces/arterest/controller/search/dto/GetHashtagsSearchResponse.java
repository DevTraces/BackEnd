package com.devtraces.arterest.controller.search.dto;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.hashtag.Hashtag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GetHashtagsSearchResponse {
	private Long totalNumberOfSearches;
	private List<FeedInfoMap> feedList;

	public static GetHashtagsSearchResponse from(Hashtag hashtag, Long totalNumberOfSearches){

		List<Feed> totalFeedList = hashtag.getFeedHashtagMapList().stream()
			.map(FeedHashtagMap -> FeedHashtagMap.getFeed()).collect(Collectors.toList());

		List<FeedInfoMap> feedList = totalFeedList.stream()
			.map(Feed -> FeedInfoMap.builder()
				.feedId(Feed.getId())
				.imageUrl(Feed.getImageUrls())
				.build())
			.collect(Collectors.toList());

		return GetHashtagsSearchResponse.builder()
			.totalNumberOfSearches(totalNumberOfSearches)
			.feedList(feedList)
			.build();
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class FeedInfoMap{
		private Long feedId;
		private String imageUrl;
	}
}
