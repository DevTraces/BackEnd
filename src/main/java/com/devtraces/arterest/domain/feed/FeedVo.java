package com.devtraces.arterest.domain.feed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedVo {
	private String hashtags;

	public FeedVo(FeedHashtagsInterface feedHashtagsInterface){
		hashtags = feedHashtagsInterface.getHashtags();
	}
}
