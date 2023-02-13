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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GetHashtagsSearchResponse {
	private Long totalNumberOfSearches;
	private List<FeedInfo> feedList;

	public static GetHashtagsSearchResponse from(Hashtag hashtag, Pageable pageable){

		List<Feed> totalFeedList = hashtag.getFeedHashtagMapList().stream()
			.map(FeedHashtagMap -> FeedHashtagMap.getFeed()).collect(Collectors.toList());

		List<FeedInfo> feedInfoList = totalFeedList.stream()
			.map(Feed -> FeedInfo.builder()
				.feedId(Feed.getId())
				.imageUrl(getFirstImageUrl(Feed.getImageUrls()))
				.build())
			.collect(Collectors.toList());

		Page<FeedInfo> feedInfoPage = new PageImpl<>(feedInfoList, pageable, feedInfoList.size());

		return GetHashtagsSearchResponse.builder()
			.totalNumberOfSearches((long) totalFeedList.size())
			.feedList(feedInfoPage.getContent())
			.build();
	}

	private static String getFirstImageUrl(String imageUrls){
		String[] imageUrlList = imageUrls.split(",");
		// 게시물의 첫번째 이미지만 전달
		return imageUrlList[0];
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class FeedInfo{
		private Long feedId;
		private String imageUrl;
	}
}
