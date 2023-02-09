package com.devtraces.arterest.common.jwt.service;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.service.user.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class JwtService {

	private final JwtProvider jwtProvider;
	private final AuthService authService;
	private final RedisService redisService;
	private final UserRepository userRepository;

	@Transactional
	public TokenDto reissue(String nickname, String accessToken, String refreshToken) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);
		validateTokens(accessToken, refreshToken, user.getId());

		if (!redisService.hasSameRefreshToken(user.getId(), refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}

		return jwtProvider.generateAccessTokenAndRefreshToken(user.getId());
	}

	private void validateTokens(String accessToken, String refreshToken, Long userId) {
		// 토큰 재발행의 경우 Access Token이 만료되어야 한다.
		if (!jwtProvider.isExpiredToken(accessToken)) {
			authService.signOut(userId, accessToken);
			throw BaseException.NOT_EXPIRED_ACCESS_TOKEN;
		}

		if (jwtProvider.isExpiredToken(refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}
	}
}
