package com.devtraces.arterest.common.utils;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO: 이름 바꾸기
@RequiredArgsConstructor
@Component
public class SecurityContextUtils {

	private final UserRepository userRepository;

	public static long parseUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 비로그인 접근
		if (authentication instanceof AnonymousAuthenticationToken) {
			throw BaseException.FORBIDDEN;
		}

		return Long.parseLong(authentication.getName());
	}

	@Transactional(readOnly = true)
	public User parseUserIdAndGetUser() {
		return userRepository.findById(parseUserId())
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);
	}
}
