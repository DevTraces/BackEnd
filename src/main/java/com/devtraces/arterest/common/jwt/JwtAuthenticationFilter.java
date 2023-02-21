package com.devtraces.arterest.common.jwt;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

	private final JwtProvider jwtProvider;
	private final TokenRedisUtil tokenRedisUtil;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		String token = resolveToken((HttpServletRequest) request);

		try {
			if (token != null && tokenRedisUtil.notExistsInAccessTokenBlackListBy(token)) {
				Authentication authentication = jwtProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT Token");
			request.setAttribute("exception", ErrorCode.EXPIRED_ACCESS_TOKEN.getCode());
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("Invalid JWT Token");
			request.setAttribute("exception", ErrorCode.INVALID_TOKEN.getCode());
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT Token");
			request.setAttribute("exception", ErrorCode.INVALID_TOKEN.getCode());
		} catch (IllegalArgumentException e) {
			log.info("JWT claims string is empty.");
			request.setAttribute("exception", ErrorCode.INVALID_TOKEN.getCode());
		}

		chain.doFilter(request, response);
	}

	// Request Header 에서 JWT 토큰 정보 추출
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
			return bearerToken.substring(TOKEN_PREFIX.length() + 1);
		}
		return null;
	}
}
