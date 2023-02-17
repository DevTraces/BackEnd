package com.devtraces.arterest.common.jwt;

import static com.devtraces.arterest.common.jwt.JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME;
import static com.devtraces.arterest.common.jwt.JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

	protected static final String REFRESH_TOKEN_SUBJECT_PREFIX = "refresh:";

	private final UserDetailsService userDetailsService;
	private final RedisService redisService;

	private final Key secretKey;

	public JwtProvider(
		UserDetailsService userDetailsService,
		RedisService redisService,
		@Value("${jwt.secret}") String secretKey
	) {
		this.userDetailsService = userDetailsService;
		this.redisService = redisService;

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
			.setSubject(REFRESH_TOKEN_SUBJECT_PREFIX + userId)	// refresh token으로 인증인가할 수 없도록 PREFIX 설정
			.setIssuedAt(now)
			.setExpiration(refreshTokenExpiredIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		redisService.setRefreshTokenValue(userId, refreshToken, refreshTokenExpiredIn);

		return TokenDto.builder()
			.accessToken(accessToken)
			.responseCookie(generateCookie(refreshToken))
			.build();
	}

	private ResponseCookie generateCookie(String refreshToken) {
		return ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.path("/refresh-token")
			.build();
	}

	// JWT 토큰에서 인증 정보 조회
	public Authentication getAuthentication(String accessToken) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(accessToken));
		return new UsernamePasswordAuthenticationToken(Long.parseLong(this.getUserId(accessToken)), "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	private String getUserId(String accessToken) {
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
