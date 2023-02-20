package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.NicknameCheckResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

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
}