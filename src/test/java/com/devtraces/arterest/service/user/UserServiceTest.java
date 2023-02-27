package com.devtraces.arterest.service.user;

import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.service.s3.S3Service;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.user.dto.response.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.response.NicknameCheckResponse;
import com.devtraces.arterest.controller.user.dto.response.ProfileByNicknameResponse;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static com.devtraces.arterest.common.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("이메일 중복 검사 성공 - 중복된 이메일인 경우 true 응답")
    void success_checkEmail_DUPLICATED_EMAIL() {
        String email = "hello@abc.com";
        User user = User.builder().email(email).build();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        EmailCheckResponse response = userService.checkEmail(email);

        assertEquals(true, response.isDuplicatedEmail());
    }

    @Test
    @DisplayName("이메일 중복 검사 성공 - 중복되지 않은 이메일인 경우 false 응답")
    void success_checkEmail_NOT_DUPLICATED_EMAIL() {
        String email = "hello@abc.com";
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        EmailCheckResponse response = userService.checkEmail(email);

        assertEquals(false, response.isDuplicatedEmail());
    }

    @Test
    @DisplayName("닉네임 중복 검사 성공 - 중복된 닉네임인 경우 true 응답")
    void success_checkNickname_DUPLICATED_NICKNAME() {
        String nickname = "duplicatedNickname";
        User user = User.builder().nickname(nickname).build();
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));

        NicknameCheckResponse response = userService.checkNickname(nickname);

        assertEquals(true, response.isDuplicatedNickname());
    }

    @Test
    @DisplayName("닉네임 중복 검사 성공 - 중복이 아닌 닉네임인 경우 false 응답")
    void success_checkNickname_NOT_DUPLICATED_NICKNAME() {
        String nickname = "notDuplicatedNickname";
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        NicknameCheckResponse response = userService.checkNickname(nickname);

        assertEquals(false, response.isDuplicatedNickname());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void success_updatePassword() {
        // given
        String beforePassword = "beforePassword";
        String afterPassword = "afterPassword";
        User user = User.builder().id(1L).password(beforePassword).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        userService.updatePassword(1L, beforePassword, afterPassword);

        // then
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals(passwordEncoder.encode(afterPassword), captor.getValue().getPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 존재하지 않는 사용자")
    void fail_updatePassword_USER_NOT_FOUND() {
        // given
        String beforePassword = "beforePassword";
        String afterPassword = "afterPassword";

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updatePassword(1L, beforePassword, afterPassword));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 기존 비밀번호")
    void fail_updatePassword_WRONG_BEFORE_PASSWORD() {
        // given
        String beforePassword = "beforePassword";
        String afterPassword = "afterPassword";
        User user = User.builder().id(1L).password(beforePassword).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updatePassword(1L, beforePassword, afterPassword));

        // then
        assertEquals(WRONG_BEFORE_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공 - 나를 팔로우하는 유저")
    void success_getProfileByNickname_USER_FOLLOWING_ME() {
        //given
        String testNickname = "nickname";
        Integer totalFeedNumber = 1;
        int followerNumber = 0;
        int followingNumber = 1;
        int isFollowing = 0;
        User targetUser = User.builder()
                .id(2L)
                .nickname(testNickname)
                .followList(new ArrayList<>())
                .build();
        User user = User.builder()
                .id(1L)
                .build();
        Follow follow = Follow.builder()
                .id(1L)
                .user(targetUser)
                .followingId(2L)
                .build();
        targetUser.getFollowList().add(follow);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(feedRepository.countAllByUserId(anyLong())).willReturn(totalFeedNumber);
        given(followRepository.countAllByFollowingId(anyLong())).willReturn(followerNumber);
        given(followRepository.isFollowing(anyLong(), anyLong())).willReturn(isFollowing);

        //when
        ProfileByNicknameResponse response =
                userService.getProfileByNickname(user.getId(), testNickname);

        //then
        assertEquals(testNickname, response.getNickname());
        assertEquals(totalFeedNumber, response.getTotalFeedNumber());
        assertEquals(followerNumber, response.getFollowerNumber());
        assertEquals(followingNumber, response.getFollowingNumber());
        assertEquals(false, response.getIsFollowing());
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공 - 내가 팔로우하는 유저")
    void success_getProfileByNickname_USER_I_FOLLOW() {
        //given
        String testNickname = "nickname";
        Integer totalFeedNumber = 1;
        int followerNumber = 1;
        int followingNumber = 0;
        int isFollowing = 1;
        User user = User.builder()
                .id(2L)
                .build();
        User targetUser = User.builder()
                .id(1L)
                .nickname(testNickname)
                .followList(new ArrayList<>())
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(targetUser));
        given(feedRepository.countAllByUserId(anyLong())).willReturn(totalFeedNumber);
        given(followRepository.countAllByFollowingId(anyLong())).willReturn(followerNumber);
        given(followRepository.isFollowing(anyLong(), anyLong())).willReturn(isFollowing);

        //when
        ProfileByNicknameResponse response =
                userService.getProfileByNickname(user.getId(), testNickname);

        //then
        assertEquals(testNickname, response.getNickname());
        assertEquals(totalFeedNumber, response.getTotalFeedNumber());
        assertEquals(followerNumber, response.getFollowerNumber());
        assertEquals(followingNumber, response.getFollowingNumber());
        assertEquals(true, response.getIsFollowing());
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 존재하지 않는 사용자")
    void fail_getProfileByNickname_USER_NOT_FOUND() {
        //given
        String testNickname = "nickname";
        User user = User.builder().id(1L).build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.empty());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> userService.getProfileByNickname(user.getId(), testNickname)
                );

        //then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void success_updateProfile() {
        //given
        String nickname = "nickname";
        String updateUsername = "updateUsername";
        String updateNickname = "updateNickname";
        String updateDescription = "updateDescription";

        User user = User.builder()
                .id(1L)
                .nickname(nickname)
                .build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.save(any())).willReturn(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        //when
       userService.updateProfile(
                user.getId(), nickname, updateUsername,
                updateNickname, updateDescription
        );

        //then
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals(updateUsername, captor.getValue().getUsername());
        assertEquals(updateNickname, captor.getValue().getNickname());
        assertEquals(updateDescription, captor.getValue().getDescription());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 존재하지 않는 사용자")
    void fail_updateProfile_USER_NOT_FOUND() {
        //given
        String nickname = "nickname";
        String updateUsername = "updateUsername";
        String updateNickname = "updateNickname";
        String updateDescription = "updateDescription";

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateProfile(
                        anyLong(), nickname, updateUsername,
                        updateNickname, updateDescription
                )
        );

        //then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 본인 프로필 아닌 경우")
    void fail_updateProfile_FORBIDDEN() {
        //given
        String ownerNickname = "ownerNickname";
        String notOwnerNickname = "notOwnerNickname";
        String updateUsername = "updateUsername";
        String updateNickname = "updateNickname";
        String updateDescription = "updateDescription";
        User notOwner = User.builder()
                .id(2L)
                .nickname(notOwnerNickname)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(notOwner));

        //when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateProfile(
                        notOwner.getId(), ownerNickname, updateUsername,
                        updateNickname, updateDescription
                )
        );

        //then
        assertEquals(FORBIDDEN, exception.getErrorCode());
    }

    @Test
    void success_updateProfileImage() {
        //given
        Long userId = 1L;
        String nickname = "nickname";
        User user = User.builder().id(userId).nickname(nickname).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.save(any())).willReturn(user);

        String profileImageUrl = "profileImageUrl";
        MockMultipartFile profileImage =
                new MockMultipartFile(
                        "profileImage", "profileImage".getBytes());
        given(s3Service.uploadImage(any())).willReturn(profileImageUrl);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        //when
        userService.updateProfileImage(userId, nickname, profileImage);

        //then
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals(profileImageUrl, captor.getValue().getProfileImageUrl());
    }

    @Test
    @DisplayName("프로필 이미지 수정 실패 - 존재하지 않는 사용자")
    void fail_updateProfileImage_USER_NOT_FOUND() {
        //given
        Long userId = 1L;
        String nickname = "nickname";
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        MockMultipartFile profileImage =
                new MockMultipartFile(
                        "profileImage", "profileImage".getBytes());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> userService.updateProfileImage(
                                userId, nickname, profileImage
                )
        );

        //then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("프로필 이미지 수정 실패 - 다른 사람의 프로필 이미지 수정")
    void fail_updateProfileImage_FORBIDDEN() {
        //given
        String ownerNickname = "ownerNickname";

        MockMultipartFile profileImage =
                new MockMultipartFile(
                        "profileImage", "profileImage".getBytes());

        Long notOwnerId = 2L;
        String notOwnerNickname = "notOwnerNickname";
        User notOwner = User.builder().id(notOwnerId).nickname(notOwnerNickname).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(notOwner));


        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> userService.updateProfileImage(
                                notOwnerId,
                                ownerNickname,
                                profileImage
                        )
                );

        //then
        assertEquals(FORBIDDEN, exception.getErrorCode());
    }
}