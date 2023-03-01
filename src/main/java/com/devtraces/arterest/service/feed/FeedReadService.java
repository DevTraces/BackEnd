package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import com.devtraces.arterest.model.user.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
	private final FeedRecommendationCacheRepository feedRecommendationCacheRepository;
	private final LikeRecommendationRepository likeRecommendationRepository;

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
			.getContent().stream().map(
			feed -> {
				Long likeNumber = getOrCacheLikeNumber(feed);
				return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
			}
		).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public FeedResponse getOneFeed(Long userId, Long feedId){
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		Feed feed = feedRepository.findById(feedId).orElseThrow(() -> BaseException.FEED_NOT_FOUND);
		Long likeNumber = getOrCacheLikeNumber(feed);

		return FeedResponse.from(
			feed, likedFeedSet, likeNumber, bookmarkedFeedSet
		);
	}

	@Transactional(readOnly = true)
	public Feed getOneFeedEntity(Long feedId){
		return feedRepository.findById(feedId).orElseThrow(
			() -> BaseException.FEED_NOT_FOUND
		);
	}

	// 최근 30일 이내에 생성된 게시물들 중에서 userId 유저가 팔로잉 하고 있는 유저가 작성한 게시물을
	// 최신 순으로 보여주고, 그 개수가 pageSize 보다 작을 경우 좋아요 개수 상위 글들을 랜덤하게 선택하여 채워 넣는다.
 	@Transactional(readOnly = true)
	public List<FeedResponse> getMainFeedList(Long userId, Integer page, Integer pageSize) {
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		List<Long> followingUserIdList = userRepository.findById(userId).orElseThrow(
			() -> BaseException.USER_NOT_FOUND
		).getFollowList().stream().map(Follow::getFollowingId)
			.collect(Collectors.toList());

		LocalDateTime to = LocalDateTime.now();
		LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

		List<FeedResponse> responseList = feedRepository
			.findAllByUserIdInAndCreatedAtBetweenOrderByCreatedAtDesc(
				followingUserIdList, from, to, PageRequest.of(page, pageSize)
			).getContent().stream().map(
				feed -> {
					Long likeNumber = getOrCacheLikeNumber(feed);
					return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
				}
			).collect(Collectors.toList());

		// responseList의 길이가 10 미만일 경우, 좋아요 개수 상위 게시물을 랜덤하게 선택하여 리스트 내용물을 추가한다.
		if(responseList.size() == 0){
			List<Long> recommendedFeedIdList;
			// 캐시 서버를 본다.
			recommendedFeedIdList = feedRecommendationCacheRepository
				.getRecommendationTargetFeedIdList();
			if(recommendedFeedIdList == null){
				// 없으면 DB를 본다.
				Optional<LikeRecommendation> optionalLikeRecommendation
					= likeRecommendationRepository.findTopByOrderByIdDesc();
				if(optionalLikeRecommendation.isPresent()){
					recommendedFeedIdList = Arrays.stream(
						optionalLikeRecommendation.get()
							.getRecommendationTargetFeeds().split(",")
					).map(Long::parseLong).collect(Collectors.toList());
				} else {
					// DB 마저도 없다면 빈 리스트를 반환한다.
					return Collections.emptyList();
				}
			}

			// 추천 feedId 리스트를 랜덤하게 섞은 후 그 중에서 상위 (pageSize - responseList.size()) 만큼을 뽑는다.
			// feedId 리스트의 길이가 (pageSize - responseList.size())보다 작다면, feedId 리스트 전체를 뽑는다.
			Collections.shuffle(recommendedFeedIdList);
			List<Long> randomlySelectedRecommendedFeedList = new ArrayList<>();
			for(Long feedId : recommendedFeedIdList){
				if(randomlySelectedRecommendedFeedList.size() != (pageSize - responseList.size()) ){
					randomlySelectedRecommendedFeedList.add(feedId);
				} else break;
			}

			List<FeedResponse> recommendedFeedList = feedRepository
				.findAllByIdInOrderByCreatedAtDesc(randomlySelectedRecommendedFeedList)
				.getContent().stream().map(
				feed -> {
					Long likeNumber = getOrCacheLikeNumber(feed);
					return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
				}
			).collect(Collectors.toList());

			responseList.addAll(recommendedFeedList);
		}
		return responseList;
	}

	// 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
	private Long getOrCacheLikeNumber(Feed feed) {
		Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feed.getId());
		if(likeNumber == null) {
			likeNumber = likeRepository.countByFeedId(feed.getId());
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
