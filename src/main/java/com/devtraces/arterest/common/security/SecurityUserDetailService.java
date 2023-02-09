package com.devtraces.arterest.common.security;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SecurityUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String userId) {
		User user = userRepository.findById(Long.parseLong(userId))
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);

		return new SecurityUser(user);
	}
}
