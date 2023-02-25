package com.devtraces.arterest.service.auth;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.controller.auth.dto.UserInfoFromKakaoDto;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OauthServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OauthService oauthService;

    @Test
    @DisplayName("카카오 소셜 로그인 성공 - 회원가입")
    void success_KakaoSignUpOrSignIn_SIGNUP() {
        //given
        UserInfoFromKakaoDto dto = UserInfoFromKakaoDto.builder()
                .kakaoUserId(320482824L)
                .email("example@abc.com")
                .username("username")
                .nickname("randomNickname")
                .profileImageUrl("profileImageUrl")
                .description("description")
                .build();

        User user = User.builder()
                .id(1L)
                .kakaoUserId(dto.getKakaoUserId())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .description(dto.getDescription())
                .userStatus(UserStatusType.ACTIVE)
                .signupType(UserSignUpType.KAKAO_TALK)
                .build();

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("accessToken")
                .cookie(
                        ResponseCookie.from("refreshToken", "refresh-token")
                                .build()
                )
                .build();

        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.empty());
        given(userRepository.findByKakaoUserId(anyLong()))
                .willReturn(Optional.empty());
        given(userRepository.save(any())).willReturn(user);
        given(jwtProvider.generateAccessTokenAndRefreshToken(user.getId()))
                .willReturn(tokenDto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        //when
        oauthService.kakaoSignUpOrSignIn(dto);

        //then
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals(dto.getKakaoUserId(), captor.getValue().getKakaoUserId());
        assertEquals(dto.getEmail(), captor.getValue().getEmail());
        assertEquals(dto.getUsername(), captor.getValue().getUsername());
        assertEquals(dto.getNickname(), captor.getValue().getNickname());
        assertEquals(dto.getProfileImageUrl(), captor.getValue().getProfileImageUrl());
        assertEquals(dto.getDescription(), captor.getValue().getDescription());
        assertEquals(UserStatusType.ACTIVE, captor.getValue().getUserStatus());
        assertEquals(UserSignUpType.KAKAO_TALK, captor.getValue().getSignupType());
    }

    @Test
    @DisplayName("카카오 소셜 로그인 성공 - 로그인")
    void success_KakaoSignUpOrSignIn_SIGNIN() {
        //given
        UserInfoFromKakaoDto dto = UserInfoFromKakaoDto.builder()
                .kakaoUserId(320482824L)
                .email("example@abc.com")
                .username("username")
                .nickname("randomNickname")
                .profileImageUrl("profileImageUrl")
                .description("description")
                .build();

        User user = User.builder()
                .id(1L)
                .kakaoUserId(dto.getKakaoUserId())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .description(dto.getDescription())
                .userStatus(UserStatusType.ACTIVE)
                .signupType(UserSignUpType.KAKAO_TALK)
                .build();

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("accessToken")
                .cookie(
                        ResponseCookie.from("refreshToken", "refresh-token")
                                .build()
                )
                .build();

        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.of(user));
        given(jwtProvider.generateAccessTokenAndRefreshToken(user.getId()))
                .willReturn(tokenDto);

        //when
        TokenWithNicknameDto response =
                oauthService.kakaoSignUpOrSignIn(dto);

        //then
        assertEquals(tokenDto.getAccessToken(), response.getAccessToken());
        assertEquals(tokenDto.getCookie(), response.getCookie());
        assertEquals(user.getNickname(), response.getNickname());
    }

    @Test
    @DisplayName("카카오 소셜 로그인 실패 - 일반 회원가입으로 가입한 유저")
    void fail_kakaoSignUpOrSignIn_ALREADY_EXIST_USER() {
        //given
        UserInfoFromKakaoDto dto = UserInfoFromKakaoDto.builder()
                .kakaoUserId(320482824L)
                .email("example@abc.com")
                .username("username")
                .nickname("randomNickname")
                .profileImageUrl("profileImageUrl")
                .description("description")
                .build();

        User user = User.builder()
                .id(1L)
                .email(dto.getEmail())
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .description(dto.getDescription())
                .userStatus(UserStatusType.ACTIVE)
                .signupType(UserSignUpType.EMAIL)
                .build();

        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> oauthService.kakaoSignUpOrSignIn(dto)
        );

        //then
        assertEquals(ErrorCode.ALREADY_EXIST_USER, exception.getErrorCode());
    }
}