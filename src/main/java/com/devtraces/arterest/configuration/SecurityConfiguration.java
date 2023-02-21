package com.devtraces.arterest.configuration;

import com.devtraces.arterest.common.jwt.JwtAccessDeniedHandler;
import com.devtraces.arterest.common.jwt.JwtAuthenticationEntryPoint;
import com.devtraces.arterest.common.jwt.JwtAuthenticationFilter;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final JwtProvider jwtProvider;
	private final TokenRedisUtil tokenRedisUtil;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.httpBasic().disable()
			.csrf().disable()
			.cors().configurationSource(corsConfigurationSource())

			.and()
			.exceptionHandling()
			.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			.accessDeniedHandler(jwtAccessDeniedHandler)

			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

			.and()
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, tokenRedisUtil),
				UsernamePasswordAuthenticationFilter.class)

			.authorizeRequests()
			.antMatchers("/api/auth/sign-up", "/api/auth/email/auth-key",
				"/api/auth/email/auth-key/check", "/api/auth/sign-in",
				"/api/oauth/kakao/callback", "/api/auth/password/check",
				"/api/auth/sign-out", "/api/auth/withdrawal",
				"/api/users/email/**", "/api/users/nickname/**",
				"/api/users/password", "/api/tokens/reissue").permitAll()//.hasRole("USER")
			.anyRequest().authenticated(); // permitAll() 로 하면 JwtAuthenticationEntryPoint 동작안함

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("OPTIONS", "HEAD", "GET", "POST", "PUT", "PATCH", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
