package com.devtraces.arterest.service.follow;

import static org.junit.jupiter.api.Assertions.*;

import com.devtraces.arterest.domain.follow.FollowRepository;
import com.devtraces.arterest.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


        // when


        // then

    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 팔로우 요청한 유저 찾지 못함.")
    void failedCreateFollowRelationFollowerUserNotFound(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 팔로우 가능 최대 숫자 초과")
    void failedCreateFollowRelationFollowLimitExceed(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("팔로우 관계 추가 실패 - 타깃 유저 찾지 못함.")
    void failedCreateFollowRelationFollowingUserNotFound(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스트 획득 성공")
    void successGetFollowingUserList(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스틓 획득 실패 - 타깃 유저 찾지 못함")
    void failedGetFollowingUserListTargetUserNotFound(){
        // given


        // when


        // then

    }

    @Test
    @DisplayName("타깃 유저의 팔로잉 리스트 획득 실패 - 요청한 유저 찾지 못함")
    void failedGetFollowingUserListRequestedUserNotFound(){
        // given


        // when


        // then

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