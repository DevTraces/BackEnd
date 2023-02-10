package com.devtraces.arterest.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.devtraces.arterest.common.component.MailUtil;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private MailUtil mailUtil;
	@Mock
	private RedisService redisService;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void testSendMailWithAuthKey() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		willDoNothing()
			.given(mailUtil).sendMail(anyString(), anyString(), anyString());
		willDoNothing()
			.given(redisService).setAuthKeyValue(anyString(), anyString());

		authService.sendMailWithAuthKey("example@gmail.com");
	}

	// 이미 가입한 이메일로 인증 코드를 요청할 경우
	@Test
	void testSendMailWithAuthKeyByRegisteredUser() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.sendMailWithAuthKey("example@gmail.com"));

		assertEquals(BaseException.ALREADY_EXIST_EMAIL, exception);
	}

	// 인증 코드 랜덤으로 6글자 잘 생성되는지 검사
	@Test
	void testGenerateAuthKey() {
		String authKey1 = authService.generateAuthKey();
		String authKey2 = authService.generateAuthKey();

		assertNotEquals(authKey1, authKey2);
		assertEquals(6, authKey1.length());
		assertEquals(6, authKey2.length());
	}

	@Test
	void testSignInAndGenerateJwtToken() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		TokenDto mockTokenDto = TokenDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.build();
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(mockTokenDto);

		authService.signInAndGenerateJwtToken("email@gmail.com", "pw");
	}

	// 조회되지 않는 이메일인 경우
	@Test
	void testSignInAndGenerateJwtTokenByWrongEmail() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.signInAndGenerateJwtToken("wrong@gmail.com", "pw"));

		assertEquals(BaseException.WRONG_EMAIL_OR_PASSWORD, exception);
	}

	// 비밀번호가 불일치하는 경우
	@Test
	void testSignInAndGenerateJwtTokenByWrongPassword() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.signInAndGenerateJwtToken("email@gmail.com", "wrongPw"));

		assertEquals(BaseException.WRONG_EMAIL_OR_PASSWORD, exception);
	}
}
