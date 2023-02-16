package com.devtraces.arterest.service.follow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.follow.dto.FollowResponse;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.follow.Follow;
import com.devtraces.arterest.domain.follow.FollowRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        given(followRepository.save(any())).willReturn(followEntity);

        // when
        followService.createFollowRelation(1L, "two");

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findByNickname(anyString());
        verify(followRepository, times(1)).save(any());
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
        for(int i=1; i<= CommonUtils.FOLLOW_COUNT_LIMIT; i++){
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


        // when


        // then

    }

    @Test
    @DisplayName("타깃 유저의 팔로워 리스트 획득 실패 - 타깃 유저 찾지 못함.")
    void failedGetFollowerUserListTargetUserNotFound(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("타깃 유저의 팔로워 리스트 획득 실패 - 요청한 유저 찾지 못함.")
    void failedGetFollowerUserListRequestedUserNotFound(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("팔로우 관계 제거 성공")
    void successDeleteFollowRelation(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("팔로우 관계 제거 실패 - 팔로잉 했던 유저 찾지 못함.")
    void failedDeleteFollowRelationUserNotFound(){
        // given


        // when


        // then

    }
}