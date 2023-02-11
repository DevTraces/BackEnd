package com.devtraces.arterest.controller.search.dto;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.hashtag.Hashtag;
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
	private Long feedId;
	private String imageUrl;

	public static GetHashtagsSearchResponse from(Hashtag hashtag){
		return GetHashtagsSearchResponse.builder()
			.feedId(hashtag.getFeed().getId())
			.imageUrl(hashtag.getFeed().getImageUrls())
			.build();
	}
}
