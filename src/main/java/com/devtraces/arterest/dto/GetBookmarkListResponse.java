package com.devtraces.arterest.dto;

import com.devtraces.arterest.domain.bookmark.Bookmark;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GetBookmarkListResponse {

	private Long feedId;
	private String imageUrl;

	public static GetBookmarkListResponse from(Bookmark bookmark){
		return GetBookmarkListResponse.builder()
			.feedId(bookmark.getFeed().getId())
			.imageUrl(getFirstImageUrl(bookmark.getFeed().getImageUrls()))
			.build();
	}

	private static String getFirstImageUrl(String imageUrls){
		String[] imageUrlList = imageUrls.split(",");
		return imageUrlList[0];
	}
}
