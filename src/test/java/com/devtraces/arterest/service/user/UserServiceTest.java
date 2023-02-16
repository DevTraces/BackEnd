package com.devtraces.arterest.service.user;

import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.NicknameCheckResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
}