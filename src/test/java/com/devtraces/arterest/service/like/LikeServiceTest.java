package com.devtraces.arterest.service.like;

import static org.junit.jupiter.api.Assertions.*;

import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        // when

        // then

    }

    @Test
    @DisplayName("좋아요 누르기 실패 - 게시물 못 찾음.")
    void failedPressLikeFeedNotFound(){
        // given

        // when

        // then

    }

    @Test
    @DisplayName("좋아요 취소 성공.")
    void successCancelLike(){
        // given

        // when

        // then

    }

    @Test
    @DisplayName("좋아요 취소 실패 - 게시물 못 찾음.")
    void failedCancelLikeFeedNotFound(){
        // given

        // when

        // then

    }

    @Test
    @DisplayName("좋아요 누른 사람 리스트 확인 성공")
    void successGetLikedUserList(){
        // given

        // when

        // then

    }

    @Test
    @DisplayName("좋아요 누른 사람 리스트 확인 실패 - 게시물 못 찾음.")
    void failedGetLikedUserListFeedNotFound(){
        // given

        // when

        // then

    }

}