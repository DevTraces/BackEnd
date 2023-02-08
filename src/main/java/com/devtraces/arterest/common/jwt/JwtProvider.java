package com.devtraces.arterest.common.jwt;

import static com.devtraces.arterest.common.jwt.JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME;
import static com.devtraces.arterest.common.jwt.JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

	private static final String REFRESH_TOKEN_SUBJECT_PREFIX = "refresh:";

	private final UserDetailsService userDetailsService;

	private final Key secretKey;

	public JwtProvider(
		UserDetailsService userDetailsService,
		@Value("${jwt.secret}") String secretKey
	) {
		this.userDetailsService = userDetailsService;

		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	// Jwt Access Token + Refresh Token 생성
	public TokenDto generateAccessTokenAndRefreshToken(Long userId) {
		Date now = new Date();

		Date accessTokenExpiresIn = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);
		String accessToken = Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(now)
			.setExpiration(accessTokenExpiresIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		Date refreshTokenExpiresIn = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);
		String refreshToken = Jwts.builder()
			.setSubject(REFRESH_TOKEN_SUBJECT_PREFIX + userId)	// refresh token으로 인증인가할 수 없도록 PREFIX 설정
			.setIssuedAt(now)
			.setExpiration(refreshTokenExpiresIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		// TODO: Refresh Token을 Redis 서버에 저장

		return TokenDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	// JWT 토큰에서 인증 정보 조회
	public Authentication getAuthentication(String accessToken) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(accessToken));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	private String getUserId(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build()
			.parseClaimsJws(token).getBody().getSubject();
	}

	// JWT 토큰 검증
	public boolean isValidateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("Invalid JWT Token");
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT Token");
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT Token");
		} catch (IllegalArgumentException e) {
			log.info("JWT claims string is empty.");
		}
		return false;
	}
}
