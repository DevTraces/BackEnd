package com.devtraces.arterest.common.jwt.service;

import static com.devtraces.arterest.common.jwt.JwtProvider.REFRESH_TOKEN_SUBJECT_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import java.util.Optional;
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
	private TokenRedisUtil tokenRedisUtil;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private JwtService jwtService;

	@Test
	void testReissue() {
		TokenDto mockTokenDto = TokenDto.builder()
			.accessTokenCookie(ResponseCookie.from("accessToken", "access-token2").build())
			.refreshTokenCookie(ResponseCookie.from("refreshToken", "refresh-token2").build())
			.build();
		given(jwtProvider.getUserId(anyString()))
			.willReturn("1");
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(tokenRedisUtil.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(true);
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(User.builder().build()));
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(mockTokenDto);

		TokenWithNicknameDto dto = jwtService.reissue("refresh-token");

		assertEquals("access-token2", dto.getAcceesTokenCookie().getValue());
		assertEquals("refresh-token2", dto.getRefreshTokenCookie().getValue());
	}

	// refresh token이 만료된 경우
	@Test
	void testReissueByExpiredRefreshToken() {

		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue( "refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}

	// Redis에 저장된 Refresh Token과 정보가 다른 경우
	@Test
	void testReissueByDifferentRefreshToken() {
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(jwtProvider.getUserId(anyString()))
			.willReturn(REFRESH_TOKEN_SUBJECT_PREFIX + "1");
		given(tokenRedisUtil.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(false);

		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("refresh-token"));

		assertEquals(BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN, exception);
	}

	// user 가 존재하지 않는 경우
	@Test
	void testReissueByUserNotFound() {
		given(jwtProvider.isExpiredToken("refresh-token"))
			.willReturn(false);
		given(jwtProvider.getUserId(anyString()))
			.willReturn(REFRESH_TOKEN_SUBJECT_PREFIX + "1");
		given(tokenRedisUtil.hasSameRefreshToken(anyLong(), anyString()))
			.willReturn(true);
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.empty());


		BaseException exception = assertThrows(BaseException.class,
			() -> jwtService.reissue("refresh-token"));

		assertEquals(BaseException.USER_NOT_FOUND, exception);
	}
}
