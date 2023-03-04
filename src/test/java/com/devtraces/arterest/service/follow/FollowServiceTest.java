package com.devtraces.arterest.service.follow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.follow.dto.response.FollowResponse;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.FollowRecommendation;
import com.devtraces.arterest.model.recommendation.FollowRecommendationRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.notice.NoticeService;
import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    @Mock
    private FollowRecommendationCacheRepository followRecommendationCacheRepository;
    @Mock
    private FollowRecommendationRepository followRecommendationRepository;
    @Mock
    private NoticeService noticeService;
    @InjectMocks
    private FollowService followService;

    @Test
    @DisplayName("팔로우 관계 추가 성공")
    void successCreateFollowRelation(){
        // given
        User requestedUser = User.builder()
            .id(1L)
            .followList(new ArrayList<>())
            .build();

        User targetUser = User.builder()
            .id(2L)
            .nickname("two")
            .build();

        Follow followEntity = Follow.builder()
            .id(1L)
            .user(requestedUser)
            .followingId(2L)
            .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(requestedUser));
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(followRepository.existsByUserIdAndFollowingId(anyLong(), anyLong())).willReturn(false);
        given(followRepository.save(any())).willReturn(followEntity);
        doNothing().when(noticeService).createFollowNotice(anyString(), anyLong());

        // when
        followService.createFollowRelation(1L, "two");

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findByNickname(anyString());
        verify(followRepository, times(1)).existsByUserIdAndFollowingId(1L, 2L);
        verify(followRepository, times(1)).save(any());
        verify(noticeService, times(1)).createFollowNotice(anyString(), anyLong());
    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 팔로우 요청한 유저 찾지 못함.")
    void failedCreateFollowRelationFollowerUserNotFound(){
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.createFollowRelation(1L, "two")
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 팔로우 가능 최대 숫자 초과")
    void failedCreateFollowRelationFollowLimitExceed(){
        // given
        User requestedUser = User.builder()
            .id(1L)
            .followList(new ArrayList<>())
            .build();

        Follow followEntity = Follow.builder().build();
        for(int i=1; i<= CommonConstant.FOLLOW_COUNT_LIMIT; i++){
            requestedUser.getFollowList().add(followEntity);
        }

        given(userRepository.findById(anyLong())).willReturn(Optional.of(requestedUser));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.createFollowRelation(1L, "two")
        );

        // then
        assertEquals(ErrorCode.FOLLOW_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 타깃 유저 찾지 못함.")
    void failedCreateFollowRelationFollowingUserNotFound(){
        // given
        User requestedUser = User.builder()
            .id(1L)
            .followList(new ArrayList<>())
            .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(requestedUser));

        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.createFollowRelation(1L, "two")
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @Disabled
    @DisplayName("팔로우 관계 추가 실패 - 이미 팔로우한 사람 또 팔로우하려 함")
    void failedCreateFollowRelationDuplicatedFollowRequest(){
        // given
        User requestedUser = User.builder()
            .id(1L)
            .followList(new ArrayList<>())
            .build();

        User targetUser = User.builder()
            .id(2L)
            .nickname("two")
            .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(requestedUser));
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(followRepository.existsByUserIdAndFollowingId(anyLong(), anyLong())).willReturn(true);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.createFollowRelation(1L, "two")
        );

        // then
        assertEquals(ErrorCode.DUPLICATED_FOLLOW_OR_LIKE, exception.getErrorCode());
    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스트 획득 성공")
    void successGetFollowingUserList(){
        // given
        User targetUser = User.builder()
            .id(1L)
            .nickname("two")
            .followList(new ArrayList<>())
            .build();

        Follow followEntityOfTargetUser = Follow.builder()
            .id(1L)
            .user(targetUser)
            .followingId(2L)
            .build();

        targetUser.getFollowList().add(followEntityOfTargetUser);

        List<Long> targetUserFollowingList = new ArrayList<>();
        targetUserFollowingList.add(2L);

        User requestedUser = User.builder()
            .id(2L)
            .followList(new ArrayList<>())
            .build();

        given(userRepository.findByNickname("two")).willReturn(Optional.of(targetUser));
        given(userRepository.findById(2L)).willReturn(Optional.of(requestedUser));

        List<User> userList = new ArrayList<>();
        userList.add(requestedUser);

        Slice<User> slice = new PageImpl<>(userList);

        given(userRepository.findAllByIdIn(targetUserFollowingList, PageRequest.of(0, 10))).willReturn(slice);

        // when
        List<FollowResponse> resultList = followService.getFollowingUserList(2L, "two", 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(userRepository, times(1)).findByNickname(anyString());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findAllByIdIn(targetUserFollowingList, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스틓 획득 실패 - 타깃 유저 찾지 못함")
    void failedGetFollowingUserListTargetUserNotFound(){
        // given
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.getFollowingUserList(1L, "two", 0, 10)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스트 획득 실패 - 요청한 유저 찾지 못함")
    void failedGetFollowingUserListRequestedUserNotFound(){
        // given
        User targetUser = User.builder()
            .id(1L)
            .nickname("two")
            .followList(new ArrayList<>())
            .build();

        Follow followEntityOfTargetUser = Follow.builder()
            .id(1L)
            .user(targetUser)
            .followingId(2L)
            .build();

        targetUser.getFollowList().add(followEntityOfTargetUser);

        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.getFollowingUserList(1L, "two", 0, 10)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("타깃 유저의 팔로워 리스트 획득 성공")
    void successGetFollowerUserList(){
        // given
        User targetUser = User.builder()
            .id(2L)
            .nickname("two")
            .followList(new ArrayList<>())
            .build();

        User requestUser = User.builder()
            .id(1L)
            .followList(new ArrayList<>())
            .build();

        Follow followEntity = Follow.builder()
            .id(1L)
            .user(requestUser)
            .followingId(2L)
            .build();

        requestUser.getFollowList().add(followEntity);

        List<Follow> followEntityList = new ArrayList<>();
        followEntityList.add(followEntity);

        Slice<Follow> slice = new PageImpl<>(followEntityList);

        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(requestUser));
        given(followRepository.findAllByFollowingId(2L, PageRequest.of(0, 10))).willReturn(slice);

        // when
        List<FollowResponse> resultList = followService.getFollowerUserList(1L, "two", 0, 10);

        // then
        assertEquals(1, resultList.size());
        verify(userRepository, times(1)).findByNickname(anyString());
        verify(userRepository, times(1)).findById(anyLong());
        verify(followRepository, times(1)).findAllByFollowingId(2L, PageRequest.of(0,10));
    }

    @Test
    @DisplayName("타깃 유저의 팔로워 리스트 획득 실패 - 타깃 유저 찾지 못함.")
    void failedGetFollowerUserListTargetUserNotFound(){
        // given
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.getFollowingUserList(1L, "two", 0, 10)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("타깃 유저의 팔로워 리스트 획득 실패 - 요청한 유저 찾지 못함.")
    void failedGetFollowerUserListRequestedUserNotFound(){
        // given
        User targetUser = User.builder()
            .id(1L)
            .nickname("two")
            .followList(new ArrayList<>())
            .build();

        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.getFollowingUserList(1L, "two", 0, 10)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 관계 제거 성공")
    void successDeleteFollowRelation(){
        // given
        User targetUser = User.builder()
            .id(1L)
            .nickname("one")
            .build();

        given(userRepository.findByNickname("one")).willReturn(Optional.of(targetUser));

        doNothing().when(followRepository).deleteByUserIdAndFollowingId(2L, 1L);
        doNothing().when(noticeService).deleteNoticeWhenFollowingCanceled(1L, 2L);

        // when
        followService.deleteFollowRelation(2L, "one");

        // then
        verify(followRepository, times(1)).deleteByUserIdAndFollowingId(2L, 1L);
    }

    @Test
    @DisplayName("팔로우 관계 제거 실패 - 팔로잉 했던 유저 찾지 못함.")
    void failedDeleteFollowRelationUserNotFound(){
        // given
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> followService.deleteFollowRelation(1L, "two")
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 샘플 캐시서버에 등록 성공")
    void successPushFollowSampleToCacheServer(){
        // given
        Follow follow = Follow.builder()
            .id(1L)
            .followingId(2L)
            .build();

        given(followRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(follow));
        doNothing().when(followSamplePoolCacheRepository).pushSample(2L);

        // when
        followService.pushFollowSampleToCacheServer();

        // then
        verify(followRepository, times(1)).findTopByOrderByIdDesc();
        verify(followSamplePoolCacheRepository, times(1)).pushSample(2L);
    }

    @Test
    @DisplayName("팔로우 추천을 위한 리스트 캐시서버에 초기화 성공")
    void successInitializeFollowRecommendationTargetUserIdListToCacheServer(){
        // given
        List<Long> recommendationList = new ArrayList<>();
        recommendationList.add(1L);
        recommendationList.add(2L);
        recommendationList.add(3L);

        doNothing().when(followRecommendationCacheRepository)
            .updateRecommendationTargetUserIdList(anyList());

        FollowRecommendation followRecommendationEntity = FollowRecommendation.builder()
            .id(1L)
            .followRecommendationTargetUsers(recommendationList.toString())
            .build();

        given(followRecommendationRepository.save(any()))
            .willReturn(followRecommendationEntity);

        // when
        followService.initializeFollowRecommendationTargetUserIdListToCacheServer();

        // then
        verify(followRecommendationCacheRepository, times(1))
            .updateRecommendationTargetUserIdList(anyList());
        verify(followRecommendationRepository, times(1))
            .save(any());
    }

    @Test
    @DisplayName("추천 리스트 획득 성공 - 캐시서버에서 획득 가능")
    void successGetRecommendationListRedisAvailable(){
        // given
        List<Long> recommendedUserIdList = new ArrayList<>();
        recommendedUserIdList.add(1L);
        recommendedUserIdList.add(2L);
        recommendedUserIdList.add(3L);

        User oneUser = User.builder()
            .id(1L)
            .build();

        User twoUser = User.builder()
            .id(2L)
            .build();

        User threeUser = User.builder()
            .id(3L)
            .build();

        List<User> userList = new ArrayList<>();
        userList.add(oneUser);
        userList.add(twoUser);
        userList.add(threeUser);

        given(followRecommendationCacheRepository
            .getFollowTargetUserIdList()).willReturn(recommendedUserIdList);
        given(userRepository.findAllByIdIn(anyList())).willReturn(userList);

        // when
        List<FollowResponse> resultList = followService.getRecommendationList();

        // then
        verify(followRecommendationCacheRepository, times(1))
            .getFollowTargetUserIdList();
        verify(userRepository, times(1)).findAllByIdIn(anyList());
        assertEquals(3, resultList.size());
    }

    @Test
    @DisplayName("추천 리스트 획득 성공 - 캐시서버에서 획득 불가능 but DB 가능")
    void successGetRecommendationListRedisNotAvailable(){
        // given
        FollowRecommendation followRecommendationEntity = FollowRecommendation.builder()
            .id(1L)
            .followRecommendationTargetUsers("1,2,3,")
            .build();

        User oneUser = User.builder()
            .id(1L)
            .build();

        User twoUser = User.builder()
            .id(2L)
            .build();

        User threeUser = User.builder()
            .id(3L)
            .build();

        List<User> userList = new ArrayList<>();
        userList.add(oneUser);
        userList.add(twoUser);
        userList.add(threeUser);

        given(followRecommendationCacheRepository
            .getFollowTargetUserIdList()).willReturn(null);
        given(followRecommendationRepository.findTopByOrderByIdDesc())
            .willReturn(Optional.of(followRecommendationEntity));
        given(userRepository.findAllByIdIn(anyList())).willReturn(userList);

        // when
        List<FollowResponse> resultList = followService.getRecommendationList();

        // then
        verify(followRecommendationCacheRepository, times(1))
            .getFollowTargetUserIdList();
        verify(userRepository, times(1)).findAllByIdIn(anyList());
        assertEquals(3, resultList.size());
    }

    @Test
    @DisplayName("추천 리스트 획득 성공 - 캐시서버 불가능 & DB 불가능")
    void successGetRecommendationListRedisNotAvailableDBEmpty(){
        // given
        given(followRecommendationCacheRepository
            .getFollowTargetUserIdList()).willReturn(null);
        given(followRecommendationRepository.findTopByOrderByIdDesc())
            .willReturn(Optional.empty());

        // when
        List<FollowResponse> resultList = followService.getRecommendationList();

        // then
        verify(followRecommendationCacheRepository, times(1))
            .getFollowTargetUserIdList();
        assertEquals(0, resultList.size());
    }

}