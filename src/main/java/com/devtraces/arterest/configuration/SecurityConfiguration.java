package com.devtraces.arterest.configuration;

import com.devtraces.arterest.common.jwt.JwtAuthenticationEntryPoint;
import com.devtraces.arterest.common.jwt.JwtAuthenticationFilter;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final JwtProvider jwtProvider;
	private final RedisService redisService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic().disable()
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http
			.authorizeRequests()
				.antMatchers("/api/auth/sign-out").hasRole("USER")
				.anyRequest().permitAll();

		http
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisService),
				UsernamePasswordAuthenticationFilter.class);

		http
			.exceptionHandling()
			.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
		return http.build();
	}
}
