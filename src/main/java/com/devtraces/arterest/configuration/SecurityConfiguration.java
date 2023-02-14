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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
			.cors().configurationSource(corsConfigurationSource()).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http
			.authorizeRequests()
				.antMatchers("/api/auth/sign-out", "api/auth/password/check").hasRole("USER")
				.anyRequest().permitAll();

		http
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisService),
				UsernamePasswordAuthenticationFilter.class);

		http
			.exceptionHandling()
			.authenticationEntryPoint(new JwtAuthenticationEntryPoint());

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
