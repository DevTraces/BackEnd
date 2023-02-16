package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.NicknameCheckResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
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
    void success_checkEmail_이메일_중복_O() {
        String email = "hello@abc.com";
        User user = User.builder().email(email).build();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        EmailCheckResponse response = userService.checkEmail(email);

        assertEquals(true, response.isDuplicatedEmail());
    }

    @Test
    void success_checkEmail_이메일_중복_X() {
        String email = "hello@abc.com";
        User user = User.builder().email(email).build();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        EmailCheckResponse response = userService.checkEmail(email);

        assertEquals(false, response.isDuplicatedEmail());
    }

    @Test
    void success_checkNickname_닉네임_중복_O() {
        String nickname = "duplicatedNickname";
        User user = User.builder().nickname(nickname).build();
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));

        NicknameCheckResponse response = userService.checkNickname(nickname);

        assertEquals(true, response.isDuplicatedNickname());
    }

    @Test
    void success_checkNickname_닉네임_중복_X() {
        String nickname = "notDuplicatedNickname";
        User user = User.builder().nickname(nickname).build();
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

        NicknameCheckResponse response = userService.checkNickname(nickname);

        assertEquals(false, response.isDuplicatedNickname());
    }

    @Test
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