package com.devtraces.arterest.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private RedisService redisService;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void testSignInAndGenerateJwtToken() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		TokenDto mockTokenDto = TokenDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.build();
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(mockTokenDto);

		authService.signInAndGenerateJwtToken("email@gmail.com", "pw");
	}

	// 조회되지 않는 이메일인 경우
	@Test
	void testSignInAndGenerateJwtTokenByWrongEmail() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.signInAndGenerateJwtToken("wrong@gmail.com", "pw"));

		assertEquals(BaseException.WRONG_EMAIL_OR_PASSWORD, exception);
	}

	// 비밀번호가 불일치하는 경우
	@Test
	void testSignInAndGenerateJwtTokenByWrongPassword() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.signInAndGenerateJwtToken("email@gmail.com", "wrongPw"));

		assertEquals(BaseException.WRONG_EMAIL_OR_PASSWORD, exception);
	}

	@Test
	void testSignOut() {
		willDoNothing()
			.given(redisService).deleteRefreshTokenBy(anyLong());
		given(jwtProvider.getExpiredDate(anyString()))
			.willReturn(new Date(new Date().getTime() + (60 * 60 * 1000)));
		willDoNothing()
			.given(redisService).setAccessTokenBlackListValue(anyString(), anyLong(), any());
	}
}
