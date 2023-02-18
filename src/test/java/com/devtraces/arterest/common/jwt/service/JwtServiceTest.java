package com.devtraces.arterest.common.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
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
import com.devtraces.arterest.service.user.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private AuthService authService;
	@Mock
	private RedisService redisService;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private JwtService jwtService;

	@Test
	void testReissue() {
		TokenDto mockTokenDto = TokenDto.builder()
			.accessToken("access-token2")
	//		.refreshToken("refresh-token2")
			.build();
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.of(createMockUser()));
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(redisService.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(true);
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(mockTokenDto);

		TokenDto tokenDto = jwtService.reissue("arterest", "access-token", "refresh-token");

		assertEquals("access-token2", tokenDto.getAccessToken());
		// assertEquals("refresh-token2", tokenDto.getRefreshToken());
	}

	// 해당 nickname으로 된 사용자 정보 없을 경우
	@Test
	void testReissueByWrongNickname() {
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("arterest", "access-token", "refresh-token"));

		assertEquals(BaseException.USER_NOT_FOUND, exception);
	}

	// 유효하지 않은 토큰인 경우
	@Test
	void testReissueByInvalidToken() {
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.of(createMockUser()));
		given(jwtProvider.isExpiredToken(anyString()))
			.willThrow(BaseException.INVALID_TOKEN);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("arterest", "access-token", "refresh-token"));

		assertEquals(BaseException.INVALID_TOKEN, exception);
	}

	// access token이 만료되지 않은 경우
	@Test
	void testReissueByNotExpiredAccessToken() {
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.of(createMockUser()));
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(false);
		willDoNothing()
			.given(authService).signOut(anyLong(), anyString());

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("arterest", "access-token", "refresh-token"));

		assertEquals(BaseException.NOT_EXPIRED_ACCESS_TOKEN, exception);
	}

	// refresh token이 만료된 경우
	@Test
	void testReissueByExpiredRefreshToken() {
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.of(createMockUser()));
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("arterest", "access-token", "refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}

	// Redis에 저장된 Refresh Token과 정보가 다른 경우
	@Test
	void testReissueByDifferentRefreshToken() {
		given(userRepository.findByNickname(anyString()))
			.willReturn(Optional.of(createMockUser()));
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(redisService.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(false);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("arterest", "access-token", "refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}

	private User createMockUser() {
		return User.builder()
			.id(1L)
			.build();
	}
}
