package com.devtraces.arterest.service.feed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponseConverter;
import com.devtraces.arterest.model.bookmark.Bookmark;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
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
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.feed.dto.FeedResponseConverterImpl;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FeedReadServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private LikeNumberCacheRepository likeNumberCacheRepository;
    @Mock
    private FeedRecommendationCacheRepository feedRecommendationCacheRepository;
    @Mock
    private LikeRecommendationRepository likeRecommendationRepository;
    @Mock
    private FollowRepository followRepository;
    @InjectMocks
    private FeedReadService feedReadService;

    @Test
    @DisplayName("피드 리스트 읽기 성공 - 레디스에서 좋아요 개수 획득 성공한 경우.")
    void successGetFeedListRedisServerAvailable(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> slice = new PageImpl<>(feedConverterList);

        given(feedRepository.findAllFeedJoinUserLatestFirst(1L, PageRequest.of(0, 10))).willReturn(slice);
        given(userRepository.findByNickname("dongvin99")).willReturn(Optional.of(user));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);
        // given(likeRepository.countByFeedId(1L)).willReturn(0L);

        // when
        List<FeedResponse> feedResponseList = feedReadService.getFeedResponseList(1L, "dongvin99", 0, 10);

        // then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(feedRepository, times(1))
            .findAllFeedJoinUserLatestFirst(1L, PageRequest.of(0, 10));
        assertEquals(feedResponseList.size(), 1);
    }

    @Test
    @DisplayName("피드 리스트 읽기 성공 - 레디스에서 좋아요 개수 획득에 실패한 경우.")
    void successGetFeedListRedisServerNotAvailable(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> slice = new PageImpl<>(feedConverterList);

        given(feedRepository.findAllFeedJoinUserLatestFirst(1L, PageRequest.of(0, 10))).willReturn(slice);
        given(userRepository.findByNickname("dongvin99")).willReturn(Optional.of(user));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setLikeNumber(1L, 0L);

        // when
        List<FeedResponse> feedResponseList = feedReadService.getFeedResponseList(1L, "dongvin99", 0, 10);

        // then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).setLikeNumber(1L, 0L);
        verify(feedRepository, times(1))
            .findAllFeedJoinUserLatestFirst(1L, PageRequest.of(0, 10));
        assertEquals(feedResponseList.size(), 1);
    }

    @Test
    @DisplayName("피드 1개 읽기 성공 - 레디스에서 좋아요 개수 획득에 성공한 경우.")
    void successGetOneFeedRedisServerAvailable(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
                1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Likes> likesList = new ArrayList<>();
        List<Bookmark> bookmarkList = new ArrayList<>();

        given(likeRepository.findAllByUserId(1L)).willReturn(likesList);
        given(bookmarkRepository.findAllByUserId(1L)).willReturn(bookmarkList);
        given(feedRepository.findOneFeedJoinUser(1L))
            .willReturn(Optional.of(feedResponseConverter));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);

        // when
        FeedResponse feedResponse = feedReadService.getOneFeed(1L, 1L);

        // then
        verify(likeRepository, times(1)).findAllByUserId(1L);
        verify(bookmarkRepository, times(1)).findAllByUserId(1L);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(feedRepository, times(1)).findOneFeedJoinUser(1L);
        assertEquals(feedResponse.getFeedId(), 1L);
    }

    @Test
    @DisplayName("피드 1개 읽기 성공 - 레디스에서 좋아요 개수 획득에 실패한 경우")
    void successGetOneFeedRedisServerNotAvailable(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Likes> likesList = new ArrayList<>();
        List<Bookmark> bookmarkList = new ArrayList<>();

        given(likeRepository.findAllByUserId(1L)).willReturn(likesList);
        given(bookmarkRepository.findAllByUserId(1L)).willReturn(bookmarkList);
        given(feedRepository.findOneFeedJoinUser(1L))
            .willReturn(Optional.of(feedResponseConverter));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setLikeNumber(1L, 0L);

        // when
        FeedResponse feedResponse = feedReadService.getOneFeed(1L, 1L);

        // then
        verify(likeRepository, times(1)).findAllByUserId(1L);
        verify(bookmarkRepository, times(1)).findAllByUserId(1L);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).setLikeNumber(1L, 0L);
        verify(feedRepository, times(1)).findOneFeedJoinUser(1L);
        assertEquals(feedResponse.getFeedId(), 1L);
    }

    @Test
    @DisplayName("게시물 엔티티 1개 찾기 성공")
    void successGetOneFeedEntity(){
        // given
        Feed feed = Feed.builder()
            .id(1L)
            .build();

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        // when
        feedReadService.getOneFeedEntity(1L);

        // then
        verify(feedRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시물 엔티티 1개 찾기 실패 - 게시물이 존재하지 않음")
    void failedGetOneFeedEntityFeedNotFound(){
        // given
        Feed feed = Feed.builder()
            .id(1L)
            .build();

        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedReadService.getOneFeedEntity(1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("메인 피드 리스트 읽기 성공 - 캐시서버 정상 동작 & 좋아요 개수 기반 게시물 추천 리스트 볼 필요 없는 경우")
    void successGetMainFeedListRedisAvailableNoNeedToReadFeedRecommendationList(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Follow> followEntityList = new ArrayList<>();
        followEntityList.add(
            Follow.builder()
                .followingId(2L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(3L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(4L)
                .build()
        );

        List<Long> followingUserIdListOfRequestUser = new ArrayList<>();
        followingUserIdListOfRequestUser.add(2L);
        followingUserIdListOfRequestUser.add(3L);
        followingUserIdListOfRequestUser.add(4L);

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> feedSlice = new PageImpl<>(feedConverterList);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime to = LocalDateTime.of(
            now.getYear(), now.getMonth(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond(),
            999999000
        );
        LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

        given(followRepository.findAllByUserId(1L)).willReturn(followEntityList);
        given(
            feedRepository.findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            )
        ).willReturn(feedSlice);

        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);

        // when
        List<FeedResponse> resultList = feedReadService.getMainFeedList(1L, 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(followRepository, times(1)).findAllByUserId(1L);
        verify(feedRepository, times(1))
            .findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            );
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
    }

    @Test
    @DisplayName("메인 피드 리스트 읽기 성공 - 캐시서버 정상 동작 & 좋아요 개수 기반 게시물 추천 리스트 봐야하는 경우")
    void successGetMainFeedListRedisAvailableNeedToReadFeedRecommendationList(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Follow> followEntityList = new ArrayList<>();
        followEntityList.add(
            Follow.builder()
                .followingId(2L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(3L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(4L)
                .build()
        );

        List<Long> followingUserIdListOfRequestUser = new ArrayList<>();
        followingUserIdListOfRequestUser.add(2L);
        followingUserIdListOfRequestUser.add(3L);
        followingUserIdListOfRequestUser.add(4L);

        List<Long> recommendedFeedIdList = new ArrayList<>();
        recommendedFeedIdList.add(1L);

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> feedSlice = new PageImpl<>(feedConverterList);

        Slice<FeedResponseConverter> emptyFeedSlice = new PageImpl<>(new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime to = LocalDateTime.of(
            now.getYear(), now.getMonth(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond(),
            999999000
        );
        LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

        given(followRepository.findAllByUserId(1L)).willReturn(followEntityList);
        given(
            feedRepository.findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            )
        ).willReturn(emptyFeedSlice);
        given(feedRecommendationCacheRepository.getRecommendationTargetFeedIdList()).willReturn(
            recommendedFeedIdList
        );
        given(feedRepository.countAllByUserIdInAndCreatedAtBetween(
            followingUserIdListOfRequestUser, from, to
        )).willReturn(0);

        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);
        given(feedRepository.findAllRecommendedFeedJoinUserLatestFirst(
            recommendedFeedIdList, PageRequest.of(0, 10)
        )).willReturn(feedSlice);

        // when
        List<FeedResponse> resultList = feedReadService.getMainFeedList(1L, 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(followRepository, times(1)).findAllByUserId(1L);
        verify(feedRepository, times(1))
            .findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            );
        verify(feedRecommendationCacheRepository, times(1))
            .getRecommendationTargetFeedIdList();
        verify(feedRepository, times(1))
            .countAllByUserIdInAndCreatedAtBetween(followingUserIdListOfRequestUser, from, to);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(feedRepository, times(1))
            .findAllRecommendedFeedJoinUserLatestFirst(recommendedFeedIdList, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("메인 피드 리스트 읽기 성공 - 캐시서버 다운 & 좋아요 개수 기반 게시물 추천 리스트 볼 필요 없는 경우")
    void successGetMainFeedListRedisNotAvailableNoNeedToReadFeedRecommendationList(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            1L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Follow> followEntityList = new ArrayList<>();
        followEntityList.add(
            Follow.builder()
                .followingId(2L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(3L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(4L)
                .build()
        );

        List<Long> followingUserIdListOfRequestUser = new ArrayList<>();
        followingUserIdListOfRequestUser.add(2L);
        followingUserIdListOfRequestUser.add(3L);
        followingUserIdListOfRequestUser.add(4L);

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> feedSlice = new PageImpl<>(feedConverterList);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime to = LocalDateTime.of(
            now.getYear(), now.getMonth(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond(),
            999999000
        );
        LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

        given(followRepository.findAllByUserId(1L)).willReturn(followEntityList);
        given(
            feedRepository.findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            )
        ).willReturn(feedSlice);

        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setLikeNumber(1L, 0L);

        // when
        List<FeedResponse> resultList = feedReadService.getMainFeedList(1L, 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(followRepository, times(1)).findAllByUserId(1L);
        verify(feedRepository, times(1))
            .findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            );
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).setLikeNumber(1L, 0L);
    }

    @Test
    @DisplayName("메인 피드 리스트 읽기 성공 - 캐시서버 다운 & 좋아요 개수 기반 게시물 추천 리스트 봐야하는 경우")
    void successGetMainFeedListRedisNotAvailableNeedToReadFeedRecommendationList(){
        // given
        FeedResponseConverter feedResponseConverter
            = new FeedResponseConverterImpl(
            5L, "donvin profile url", "donvin99",
            "게시물 내용", "imageUrl1,", "#pizza", 0,
            LocalDateTime.now(), LocalDateTime.now()
        );

        List<Follow> followEntityList = new ArrayList<>();
        followEntityList.add(
            Follow.builder()
                .followingId(2L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(3L)
                .build()
        );
        followEntityList.add(
            Follow.builder()
                .followingId(4L)
                .build()
        );

        List<Long> followingUserIdListOfRequestUser = new ArrayList<>();
        followingUserIdListOfRequestUser.add(2L);
        followingUserIdListOfRequestUser.add(3L);
        followingUserIdListOfRequestUser.add(4L);

        List<Long> recommendedFeedIdList = new ArrayList<>();
        recommendedFeedIdList.add(5L);

        LikeRecommendation likeRecommendation = LikeRecommendation.builder()
            .RecommendationTargetFeeds("5,")
            .build();

        List<FeedResponseConverter> feedConverterList = new ArrayList<>();
        feedConverterList.add(feedResponseConverter);

        Slice<FeedResponseConverter> feedSlice = new PageImpl<>(feedConverterList);

        Slice<FeedResponseConverter> emptyFeedSlice = new PageImpl<>(new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime to = LocalDateTime.of(
            now.getYear(), now.getMonth(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond(),
            999999000
        );
        LocalDateTime from = to.minusDays(CommonConstant.FEED_CONSTRUCT_DURATION_DAY);

        given(followRepository.findAllByUserId(1L)).willReturn(followEntityList);
        given(
            feedRepository.findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            )
        ).willReturn(emptyFeedSlice);
        given(feedRecommendationCacheRepository.getRecommendationTargetFeedIdList()).willReturn(
            null
        );
        given(likeNumberCacheRepository.getFeedLikeNumber(5L)).willReturn(null);
        given(likeRecommendationRepository.findTopByOrderByIdDesc()).willReturn(
            Optional.of(likeRecommendation)
        );

        given(feedRepository.countAllByUserIdInAndCreatedAtBetween(
            followingUserIdListOfRequestUser, from, to
        )).willReturn(0);

        given(likeRepository.countByFeedId(5L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setLikeNumber(5L, 0L);
        given(feedRepository.findAllRecommendedFeedJoinUserLatestFirst(
            recommendedFeedIdList, PageRequest.of(0, 10)
        )).willReturn(feedSlice);

        // when
        List<FeedResponse> resultList = feedReadService.getMainFeedList(1L, 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(followRepository, times(1)).findAllByUserId(1L);
        verify(feedRepository, times(1))
            .findAllMainFeedJoinUserLatestFirst(
                followingUserIdListOfRequestUser, from, to, PageRequest.of(0, 10)
            );
        verify(feedRecommendationCacheRepository, times(1))
            .getRecommendationTargetFeedIdList();
        verify(feedRepository, times(1))
            .countAllByUserIdInAndCreatedAtBetween(followingUserIdListOfRequestUser, from, to);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(5L);
        verify(likeRecommendationRepository, times(1))
            .findTopByOrderByIdDesc();
        verify(feedRepository, times(1))
            .findAllRecommendedFeedJoinUserLatestFirst(recommendedFeedIdList, PageRequest.of(0, 10));
        verify(likeRepository, times(1)).countByFeedId(5L);
        verify(likeNumberCacheRepository, times(1)).setLikeNumber(5L, 0L);
    }

    @Test
    @Disabled
    @DisplayName("메인 피드 리스트 읽기 실패 - 유저 정보 찾지 못함 - 불필요하여 무시함.")
    void failedGetMainFeedListUserNotFound(){
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedReadService.getMainFeedList(1L, 0, 10)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

}