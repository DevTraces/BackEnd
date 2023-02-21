package com.devtraces.arterest.common.jwt;

import static com.devtraces.arterest.common.jwt.JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.common.security.SecurityUser;
import com.devtraces.arterest.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class JwtProviderTest {

	@Mock
	private UserDetailsService userDetailsService;
	@Mock
	private RedisService redisService;
	@Value("${jwt.secret}")
	private String secretKeyString;
	private JwtProvider jwtProvider;
	private Key secretKey;

	@BeforeEach
	private void initEach() {
		jwtProvider = new JwtProvider(userDetailsService, redisService, secretKeyString);

		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@Test
	void testGenerateAccessTokenAndRefreshToken() {
		willDoNothing()
			.given(redisService).setRefreshTokenValue(anyLong(), anyString(), any());

		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);

		Claims accessTokenBody = Jwts.parserBuilder().setSigningKey(secretKey).build()
			.parseClaimsJws(tokenDto.getAccessToken()).getBody();
		assertEquals("1", accessTokenBody.getSubject());
		assertTrue(accessTokenBody.getExpiration().after(new Date()));

		Claims refreshTokenBody = Jwts.parserBuilder().setSigningKey(secretKey).build()
			.parseClaimsJws(tokenDto.getResponseCookie().getValue()).getBody();
		assertEquals(JwtProvider.REFRESH_TOKEN_SUBJECT_PREFIX + "1", refreshTokenBody.getSubject());
		assertTrue(refreshTokenBody.getExpiration().after(new Date()));
	}

	@Test
	void testGetAuthentication() {
		UserDetails mockUserDetails = new SecurityUser(User.builder().id(1L).password("pw").build());
		given(userDetailsService.loadUserByUsername(anyString()))
			.willReturn(mockUserDetails);

		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = tokenDto.getAccessToken();

		Authentication authentication = jwtProvider.getAuthentication(accessToken);

		assertEquals("1", authentication.getName());
	}

	@Test
	void testGetExpiredDate() {
		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = tokenDto.getAccessToken();

		Date expiredDate = jwtProvider.getExpiredDate(accessToken);

		Date currentExpiredIn = new Date(new Date().getTime() + ACCESS_TOKEN_EXPIRATION_TIME);
		assertTrue(expiredDate.after(new Date()));
		assertTrue(expiredDate.before(currentExpiredIn));
	}

	@Test
	void testIsExpiredToken() {
		String accessToken = Jwts.builder()
			.setSubject(String.valueOf(1L))
			.setExpiration(new Date())
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		boolean isExpiredToken = jwtProvider.isExpiredToken(accessToken);

		assertTrue(isExpiredToken);
	}

	@Test
	void testIsExpiredTokenByNotExpiredToken() {
		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = tokenDto.getAccessToken();

		boolean isExpiredToken = jwtProvider.isExpiredToken(accessToken);

		assertFalse(isExpiredToken);
	}

	@Test
	void testIsExpiredTokenByInvalidToken() {
		BaseException exception = assertThrows(BaseException.class,
			() -> jwtProvider.isExpiredToken("invalid-token"));

		assertEquals(BaseException.INVALID_TOKEN, exception);
	}
}
