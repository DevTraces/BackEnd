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
import com.devtraces.arterest.service.user.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private AuthService authService;
	@Mock
	private RedisService redisService;

	@InjectMocks
	private JwtService jwtService;

	@Test
	void testReissue() {
		TokenDto mockTokenDto = TokenDto.builder()
			.accessToken("access-token2")
			.responseCookie(ResponseCookie.from("freshToken", "refresh-token2").build())
			.build();
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(redisService.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(true);
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(mockTokenDto);

		TokenDto tokenDto = jwtService.reissue( "access-token", "refresh-token");

		assertEquals("access-token2", tokenDto.getAccessToken());
		assertEquals("refresh-token2", tokenDto.getResponseCookie().getValue());
	}

	// 유효하지 않은 토큰인 경우
	@Test
	void testReissueByInvalidToken() {
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken(anyString()))
			.willThrow(BaseException.INVALID_TOKEN);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("access-token", "refresh-token"));

		assertEquals(BaseException.INVALID_TOKEN, exception);
	}

	// access token이 만료되지 않은 경우
	@Test
	void testReissueByNotExpiredAccessToken() {
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(false);
		willDoNothing()
			.given(authService).signOut(anyLong(), anyString());

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue( "access-token", "refresh-token"));

		assertEquals(BaseException.NOT_EXPIRED_ACCESS_TOKEN, exception);
	}

	// refresh token이 만료된 경우
	@Test
	void testReissueByExpiredRefreshToken() {
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue( "access-token", "refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}

	// Redis에 저장된 Refresh Token과 정보가 다른 경우
	@Test
	void testReissueByDifferentRefreshToken() {
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken("access-token"))
			.willReturn(true);
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(redisService.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(false);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("access-token", "refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}
}
