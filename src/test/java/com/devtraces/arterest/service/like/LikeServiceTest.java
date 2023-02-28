package com.devtraces.arterest.service.like;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.like.dto.response.LikeResponse;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.like.Likes;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private LikeNumberCacheRepository likeNumberCacheRepository;
    @Mock
    private FeedRepository feedRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("좋아요 누르기 성공.")
    void successPressLike(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(true);

        Likes likeEntity = Likes.builder()
            .id(1L)
            .userId(1L)
            .feedId(1L)
            .build();

        given(likeRepository.save(any())).willReturn(likeEntity);
        doNothing().when(likeNumberCacheRepository).plusOneLike(1L);

        // when
        likeService.pressLikeOnFeed(1L ,1L);

        // then
        verify(feedRepository, times(1)).existsById(1L);
        verify(likeRepository, times(1)).save(any());
        verify(likeNumberCacheRepository, times(1)).plusOneLike(1L);
    }

    @Test
    @DisplayName("좋아요 누르기 실패 - 게시물 못 찾음.")
    void failedPressLikeFeedNotFound(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(false);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> likeService.pressLikeOnFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 취소 성공. - 레디스 정상 작동")
    void successCancelLikeRedisAvailable(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(true);
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(1L);
        doNothing().when(likeRepository).deleteByUserIdAndFeedId(anyLong(), anyLong());
        doNothing().when(likeNumberCacheRepository).minusOneLike(anyLong());

        // when
        likeService.cancelLikeOnFeed(1L ,1L);

        // then
        verify(feedRepository, times(1)).existsById(1L);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).deleteByUserIdAndFeedId(1L, 1L);
        verify(likeNumberCacheRepository, times(1)).minusOneLike(1L);
    }

    @Test
    @DisplayName("좋아요 취소 성공. - 레디스 작동 불가")
    void successCancelLikeRedisNotAvailable(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(true);
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(1L);
        doNothing().when(likeRepository).deleteByUserIdAndFeedId(anyLong(), anyLong());
        doNothing().when(likeNumberCacheRepository).minusOneLike(anyLong());

        // when
        likeService.cancelLikeOnFeed(1L ,1L);

        // then
        verify(feedRepository, times(1)).existsById(1L);
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeRepository, times(1)).deleteByUserIdAndFeedId(1L, 1L);
        verify(likeNumberCacheRepository, times(1)).minusOneLike(1L);
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 게시물 못 찾음.")
    void failedCancelLikeFeedNotFound(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(false);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> likeService.cancelLikeOnFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 좋아요 개수가 0 개임.")
    void failedCancelLikeZeroLikeNumber(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(true);
        given(likeNumberCacheRepository.getFeedLikeNumber(anyLong())).willReturn(0L);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> likeService.cancelLikeOnFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.LIKE_NUMBER_BELLOW_ZERO, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 누른 사람 리스트 확인 성공")
    void successGetLikedUserList(){
        //given
        given(feedRepository.existsById(1L)).willReturn(true);

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Likes likeEntity = Likes.builder()
            .id(1L)
            .userId(1L)
            .feedId(1L)
            .build();

        List<Likes> likesList = new ArrayList<>();
        likesList.add(likeEntity);

        Slice<Likes> likeSlice = new PageImpl<>(likesList);

        List<Long> userIdList = new ArrayList<>();
        userIdList.add(1L);

        List<User> userEntityList = new ArrayList<>();
        userEntityList.add(user);

        given(likeRepository.findAllByFeedIdOrderByCreatedAtDesc(1L, PageRequest.of(0,10))).willReturn(likeSlice);
        given(userRepository.findAllByIdIn(userIdList)).willReturn(userEntityList);

        //when
        List<LikeResponse> responseList = likeService.getLikedUserList(1L, 1L, 0, 10);

        //then
        verify(feedRepository, times(1)).existsById(1L);
        verify(likeRepository, times(1)).findAllByFeedIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));
        verify(userRepository, times(1)).findAllByIdIn(userIdList);
        assertEquals(responseList.size(), 1);
    }

    @Test
    @DisplayName("좋아요 누른 사람 리스트 확인 실패 - 게시물 못 찾음.")
    void failedGetLikedUserListFeedNotFound(){
        // given
        given(feedRepository.existsById(anyLong())).willReturn(false);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> likeService.getLikedUserList(1L, 1L, 0, 10)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 삭제시 좋아요 관련 정보 삭제 성공")
    void successDeleteFeedRelatedLikeData(){
        // given
        doNothing().when(likeNumberCacheRepository).deleteLikeNumberInfo(1L);
        doNothing().when(likeRepository).deleteAllByFeedId(1L);

        // when
        likeService.deleteLikeRelatedData(1L);

        // then
        verify(likeNumberCacheRepository, times(1)).deleteLikeNumberInfo(1L);
        verify(likeRepository).deleteAllByFeedId(1L);
    }

}