package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.UserRepository;
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
	private final UserRepository userRepository;
	private final LikeNumberCacheRepository likeNumberCacheRepository;

	@Transactional(readOnly = true)
	public List<FeedResponse> getFeedResponseList(
		Long userId, String nickname, int page, int pageSize
	){
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		Long targetUserId = userRepository.findByNickname(nickname)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND).getId();
		return feedRepository
			.findAllByUserIdOrderByCreatedAtDesc(targetUserId, PageRequest.of(page, pageSize))
			.stream().map(
			feed -> {
				Long likeNumber = getOrCacheLikeNumber(feed.getId(), feed);
				return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
			}
		).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public FeedResponse getOneFeed(Long userId, Long feedId){
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> BaseException.FEED_NOT_FOUND);
		Long likeNumber = getOrCacheLikeNumber(feedId, feed);

		return FeedResponse.from(
			feed, likedFeedSet, likeNumber, bookmarkedFeedSet
		);
	}

	// 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
	private Long getOrCacheLikeNumber(Long feedId, Feed feed) {
		Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedId);
		if(likeNumber == null) {
			likeNumber = likeRepository.countByFeedId(feedId);
			likeNumberCacheRepository.setLikeNumber(feed.getId(), likeNumber);
		}
		return likeNumber;
	}

	// 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
	private Set<Long> getLikedFeedSet(Long userId) {
		return likeRepository.findAllByUserId(userId)
			.stream().map(Likes::getFeedId).collect(Collectors.toSet());
	}

	// 요청한 사용자가 북마크 했던 피드들의 주키 아이디 번호들도 불러온다.
	private Set<Long> getBookmarkedFeedSet(Long userId) {
		return bookmarkRepository.findAllByUserId(userId)
			.stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());
	}
}
