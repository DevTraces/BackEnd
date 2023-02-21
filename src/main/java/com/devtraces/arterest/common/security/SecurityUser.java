package com.devtraces.arterest.common.security;

import com.devtraces.arterest.model.user.User;
import org.springframework.security.core.authority.AuthorityUtils;

public class SecurityUser extends org.springframework.security.core.userdetails.User {

	public SecurityUser(User user) {
		super(String.valueOf(user.getId()), user.getPassword(),
			AuthorityUtils.createAuthorityList("ROLE_USER"));
	}
}
