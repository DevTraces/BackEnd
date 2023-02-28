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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
public class JwtService {

	private final JwtProvider jwtProvider;
	private final TokenRedisUtil tokenRedisUtil;
	private final UserRepository userRepository;

	@Transactional
	public TokenWithNicknameDto reissue(String refreshToken) {

		validateTokens(refreshToken);

		Long userId = Long.parseLong(jwtProvider.getUserId(refreshToken)
			.replace(REFRESH_TOKEN_SUBJECT_PREFIX, ""));

		if (!tokenRedisUtil.hasSameRefreshToken(userId, refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> BaseException.USER_NOT_FOUND);

		return TokenWithNicknameDto.from(
				user.getNickname(),
				jwtProvider.generateAccessTokenAndRefreshToken(userId)
		);
	}

	private void validateTokens(String refreshToken) {
		if (jwtProvider.isExpiredToken(refreshToken)) {
			throw BaseException.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN;
		}
	}

}
