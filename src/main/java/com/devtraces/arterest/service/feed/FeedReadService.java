package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedReadService {

	private final FeedRepository feedRepository;
	private final LikeRepository likeRepository;
	private final BookmarkRepository bookmarkRepository;
	private final LikeNumberCacheRepository likeNumberCacheRepository;

	@Transactional(readOnly = true)
	public List<FeedResponse> getFeedResponseList(Long userId, int page, int pageSize){
		// 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
		Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
			.stream().map(Likes::getFeedId).collect(Collectors.toSet());

		// 요청한 사용자가 북마크 했던 피드들의 주키 아이디 번호들도 불러온다.
		Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
			.stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

		// 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
		return feedRepository.findAllByUserId(userId, PageRequest.of(page, pageSize)).stream().map(
			feed -> {
				Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feed.getId());
				if(likeNumber == null) {
					likeNumber = likeRepository.countByFeedId(feed.getId());
					// 현재 캐시서버에 좋아요 개수가 기록돼 있지 않으므로 다음 read 요쳥을 위해서 캐시해 둠.
					likeNumberCacheRepository.setInitialLikeNumber(likeNumber);
				}
				return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
			}
		).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public FeedResponse getOneFeed(Long userId, Long feedId){
		Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
			.stream().map(Likes::getFeedId).collect(Collectors.toSet());

		Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
			.stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> BaseException.FEED_NOT_FOUND);

		Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedId);
		if(likeNumber == null) {
			likeNumber = likeRepository.countByFeedId(feedId);
			likeNumberCacheRepository.setInitialLikeNumber(likeNumber);
		}

		return FeedResponse.from(
			feed, likedFeedSet, likeNumber, bookmarkedFeedSet
		);
	}
}
