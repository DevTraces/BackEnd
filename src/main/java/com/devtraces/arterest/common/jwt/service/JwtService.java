package com.devtraces.arterest.common.jwt.service;

import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;
import static com.devtraces.arterest.common.jwt.JwtProvider.REFRESH_TOKEN_SUBJECT_PREFIX;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.auth.AuthService;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService {

	private final JwtProvider jwtProvider;
	private final TokenRedisUtil tokenRedisUtil;
	private final UserRepository userRepository;

	@Transactional
	public TokenWithNicknameDto reissue(String bearerToken, String refreshToken) {
		log.error("check 3");
		String accessToken = "";
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
			accessToken = bearerToken.substring(TOKEN_PREFIX.length() + 1);
		}
		log.error("check 4");

		Long userId = Long.parseLong(jwtProvider.getUserId(refreshToken)
			.replace(REFRESH_TOKEN_SUBJECT_PREFIX, ""));
		validateTokens(accessToken, refreshToken, userId);

		log.error("check 5");
		if (!tokenRedisUtil.hasSameRefreshToken(userId, refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}

		log.error("check 6");
		User user = userRepository.findById(userId)
				.orElseThrow(() -> BaseException.USER_NOT_FOUND);

		log.error("check 7");
		return TokenWithNicknameDto.from(
				user.getNickname(),
				jwtProvider.generateAccessTokenAndRefreshToken(userId)
		);
	}

	private void validateTokens(String accessToken, String refreshToken, Long userId) {
		// 토큰 재발행의 경우 Access Token이 만료되어야 한다.
		if (!jwtProvider.isExpiredToken(accessToken)) {
			throw BaseException.NOT_EXPIRED_ACCESS_TOKEN;
		}

		if (jwtProvider.isExpiredToken(refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}
	}

}
