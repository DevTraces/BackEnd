package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.component.S3Util;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.NicknameCheckResponse;
import com.devtraces.arterest.controller.user.dto.ProfileByNicknameResponse;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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
    private PasswordEncoder passwordEncoder;
    @Mock
    private S3Util s3Util;
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
    @DisplayName("사용자 프로필 조회 성공")
    void success_getProfileByNickname() {
        //given
        String testNickname = "nickname";
        Integer totalFeedNumber = 1;
        User user = User.builder().id(1L).nickname(testNickname).build();

        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.of(user));
        given(feedRepository.countAllByUserId(1L))
                .willReturn(totalFeedNumber);

        //when
        ProfileByNicknameResponse response =
                userService.getProfileByNickname(testNickname);

        //then
        assertEquals(testNickname, response.getNickname());
        assertEquals(totalFeedNumber, response.getTotalFeedNumber());
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 존재하지 않는 사용자")
    void fail_getProfileByNickname_USER_NOT_FOUND() {
        //given
        String testNickname = "nickname";
        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.empty());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> userService.getProfileByNickname(testNickname)
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
        String updateProfileImageUrl = "updateProfileImageUrl";
        MultipartFile multipartFile =
                new MockMultipartFile("file", "fileContent".getBytes());
        User user = User.builder()
                .id(1L)
                .nickname(nickname)
                .build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(s3Util.uploadImage(multipartFile)).willReturn(updateProfileImageUrl);
        given(userRepository.save(any())).willReturn(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        //when
       userService.updateProfile(
                user.getId(), nickname, updateUsername,
                updateNickname, updateDescription, multipartFile
        );

        //then
        verify(userRepository, times(1)).save(captor.capture());
        verify(s3Util, times(1)).uploadImage(any());
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
        MultipartFile multipartFile =
                new MockMultipartFile("file", "fileContent".getBytes());

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateProfile(
                        anyLong(), nickname, updateUsername,
                        updateNickname, updateDescription, multipartFile
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
        MultipartFile multipartFile =
                new MockMultipartFile("file", "fileContent".getBytes());
        User notOwner = User.builder()
                .id(2L)
                .nickname(notOwnerNickname)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(notOwner));

        //when
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateProfile(
                        notOwner.getId(), ownerNickname, updateUsername,
                        updateNickname, updateDescription, multipartFile
                )
        );

        //then
        assertEquals(FORBIDDEN, exception.getErrorCode());
    }
}