package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.converter.FeedResponseConverter;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.FeedRecommendationCacheRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.recommendation.LikeRecommendation;
import com.devtraces.arterest.model.recommendation.LikeRecommendationRepository;
import com.devtraces.arterest.model.user.UserRepository;
import java.time.LocalDateTime;
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
	private final FollowRepository followRepository;

	@Transactional(readOnly = true)
	public List<FeedResponse> getFeedResponseList(
		Long userId, String nickname, int page, int pageSize
	){
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		Long targetUserId = userRepository.findByNickname(nickname)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND).getId();
		return feedRepository
			.findAllFeedJoinUserLatestFirst(targetUserId, PageRequest.of(page, pageSize))
			.getContent().stream().map(
				feedResponseConverter -> {
					Long likeNumber = getOrCacheLikeNumber(feedResponseConverter);
					return FeedResponse.fromConverter(
						feedResponseConverter, likedFeedSet, likeNumber, bookmarkedFeedSet
					);
				}
			).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public FeedResponse getOneFeed(Long userId, Long feedId){
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);

		FeedResponseConverter feedConverter = feedRepository.findOneFeedJoinUser(
			feedId
		).orElseThrow(() -> BaseException.FEED_NOT_FOUND);

		Long likeNumber = getOrCacheLikeNumber(feedConverter);

		return FeedResponse.fromConverter(
			feedConverter, likedFeedSet, likeNumber, bookmarkedFeedSet
		);
	}

	@Transactional(readOnly = true)
	public Feed getOneFeedEntity(Long feedId){
		return feedRepository.findById(feedId).orElseThrow(
			() -> BaseException.FEED_NOT_FOUND
		);
	}

	// 최근 30일 이내에 생성된 게시물들 중에서 userId 유저가 팔로잉 하고 있는 유저가 작성한 게시물을
	// 먼저 찾아내는 것이 첫 번째 쿼리이고,
	// 첫 번째 쿼리의 결과물 요소의 개수가 0일 때, 좋아요 개수 기반 추천 게시물 리스트에 있는 게시물들을 최신순으로
	// 찾아내는 것이 두 번째 쿼리다.
 	@Transactional(readOnly = true)
	public List<FeedResponse> getMainFeedList(Long userId, Integer page, Integer pageSize) {
		Set<Long> likedFeedSet = getLikedFeedSet(userId);
		Set<Long> bookmarkedFeedSet = getBookmarkedFeedSet(userId);
		List<Long> followingUserIdList = followRepository.findAllByUserId(userId).stream()
			.map(Follow::getFollowingId).collect(Collectors.toList());

		LocalDateTime now = LocalDateTime.now();

		LocalDateTime to = LocalDateTime.of(
			now.getYear(), now.getMonth(), now.getDayOfMonth(),
			now.getHour(), now.getMinute(), now.getSecond(),
			999999000
		);
		LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

		List<FeedResponse> responseList = feedRepository
			.findAllMainFeedJoinUserLatestFirst(
				followingUserIdList, from, to, PageRequest.of(page, pageSize)
			).getContent().stream().map(
				feedResponseConverter -> {
					Long likeNumber = getOrCacheLikeNumber(feedResponseConverter);
					return FeedResponse.fromConverter(
						feedResponseConverter, likedFeedSet, likeNumber, bookmarkedFeedSet
					);
				}
			).collect(Collectors.toList());

		if(responseList.size() > 0){
			// 첫 번째 쿼리의 결과물이 존재하는 경우, 좋아요 개수 기반 추천 게시물 리스트를 찾지 않고 바로 리턴.
			return responseList;
		} else {
			// 첫 번째 쿼리의 결과물 개수가 0일 경우, 좋아요 개수 상위 게시물을 찾아내는 두 번째 쿼리를 실행한다.

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

			// responseList의 길이가 최초로 0이 되는 페이지 번호를 알아낸다.
			int numberOfElemsInFirstQuery = feedRepository.countAllByUserIdInAndCreatedAtBetween(
				followingUserIdList, from, to
			);

			// (요소 숫자 + 페이지 사이즈 - 1) / 페이지 사이즈
			// 첫 번째 쿼리의 내용물들 전체를 pageSize 만큼의 용량을 가지는 페이지들로 나타내기 위해서 필요로 하는
			// 페이지 개수를 뜻한다.
			int numberOfRequiredPagesForFirstQuery
				= (numberOfElemsInFirstQuery + pageSize - 1)/pageSize;

			return feedRepository
				.findAllRecommendedFeedJoinUserLatestFirst(
					recommendedFeedIdList,
					PageRequest.of(page - numberOfRequiredPagesForFirstQuery, pageSize)
				).getContent().stream().map(
					feedResponseConverter -> {
						Long likeNumber = getOrCacheLikeNumber(feedResponseConverter);
						return FeedResponse.fromConverter(
							feedResponseConverter, likedFeedSet, likeNumber, bookmarkedFeedSet
						);
					}
				).collect(Collectors.toList());
		}
	}

	// 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
	private Long getOrCacheLikeNumber(FeedResponseConverter feedConverter) {
		Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedConverter.getFeedId());
		if(likeNumber == null) {
			likeNumber = likeRepository.countByFeedId(feedConverter.getFeedId());
			likeNumberCacheRepository.setLikeNumber(feedConverter.getFeedId(), likeNumber);
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
