package com.devtraces.arterest.common.jwt;

import static com.devtraces.arterest.common.jwt.JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME;
import static com.devtraces.arterest.common.jwt.JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

	public static final String REFRESH_TOKEN_SUBJECT_PREFIX = "refresh:";

	public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
	public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
	private static final int CREATE_AGE = 7 * 24 * 60 * 60;

	private final UserDetailsService userDetailsService;
	private final TokenRedisUtil tokenRedisUtil;

	private final Key secretKey;

	public JwtProvider(
		UserDetailsService userDetailsService,
		TokenRedisUtil tokenRedisUtil,
		@Value("${jwt.secret}") String secretKey
	) {
		this.userDetailsService = userDetailsService;
		this.tokenRedisUtil = tokenRedisUtil;

		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	// Jwt Access Token + Refresh Token 생성 및 Redis에 Refresh Token 저장
	public TokenDto generateAccessTokenAndRefreshToken(Long userId) {
		Date now = new Date();

		Date accessTokenExpiredIn = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);
		String accessToken = Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(now)
			.setExpiration(accessTokenExpiredIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		Date refreshTokenExpiredIn = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);
		String refreshToken = Jwts.builder()
			.setSubject(REFRESH_TOKEN_SUBJECT_PREFIX + userId)
			.setIssuedAt(now)
			.setExpiration(refreshTokenExpiredIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		tokenRedisUtil.setRefreshTokenValue(userId, refreshToken, refreshTokenExpiredIn);

		return TokenDto.builder()
			.accessTokenCookie(generateCookie(accessToken, ACCESS_TOKEN_COOKIE_NAME))
			.refreshTokenCookie(generateCookie(refreshToken, REFRESH_TOKEN_COOKIE_NAME))
			.build();
	}

	private ResponseCookie generateCookie(String token, String name) {

		ResponseCookie cookie = ResponseCookie.from(name, token)
			.httpOnly(true)
			.path("/")
			.maxAge(CREATE_AGE)
			.secure(true)
			.sameSite(SameSite.NONE.attributeValue())
			.build();

		return cookie;
	}

	public ResponseCookie deleteCookie(String token, String name) {

		ResponseCookie cookie = ResponseCookie.from(name, token)
			.httpOnly(true)
			.path("/")
			.maxAge(0)
			.secure(true)
			.sameSite(SameSite.NONE.attributeValue())
			.build();

		return cookie;
	}

	// JWT 토큰에서 인증 정보 조회
	public Authentication getAuthentication(String accessToken) {
		String userId = this.getUserId(accessToken);
		UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
		return new UsernamePasswordAuthenticationToken(Long.parseLong(userId), "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	public String getUserId(String accessToken) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build()
			.parseClaimsJws(accessToken).getBody().getSubject();
	}

	// JWT 토큰에서 만료 기간 정보 추출
	public Date getExpiredDate(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build()
			.parseClaimsJws(token).getBody().getExpiration();
	}

	// 만료된 JWT 토큰인지 검증
	public boolean isExpiredToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
			return false;
		} catch (ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			throw BaseException.INVALID_TOKEN;
		}
	}
}
