package com.devtraces.arterest.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.UserSignUpType;
import com.devtraces.arterest.common.UserStatusType;
import com.devtraces.arterest.common.component.MailUtil;
import com.devtraces.arterest.common.component.S3Uploader;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.redis.service.RedisService;
import com.devtraces.arterest.controller.user.dto.UserRegistrationRequest;
import com.devtraces.arterest.controller.user.dto.UserRegistrationResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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
	private S3Uploader s3Uploader;
	@Mock
	private MailUtil mailUtil;
	@Mock
	private RedisService redisService;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void testRegister() {
		User mockUser = User.builder()
			.email("test@gmail.com")
			.password("encoding-password")
			.nickname("test")
			.username("김검사")
			.signupType(UserSignUpType.EMAIL)
			.userStatus(UserStatusType.ACTIVE)
			.build();
		given(redisService.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(userRepository.existsByNickname(anyString()))
			.willReturn(false);
		given(passwordEncoder.encode(anyString()))
			.willReturn("encodingpassword");
		given(userRepository.save(ArgumentMatchers.any(User.class)))
			.willReturn(mockUser);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		UserRegistrationRequest request = UserRegistrationRequest.builder()
			.email("example@gmail.com")
			.password("password")
			.nickname("example")
			.username("김공공")
			.build();
		UserRegistrationResponse response = authService.register(request);

		verify(userRepository, times(1)).save(captor.capture());
		User savedUser = captor.getValue();
		assertEquals("example@gmail.com", savedUser.getEmail());
		assertEquals("encodingpassword", savedUser.getPassword());
		assertEquals("example", savedUser.getNickname());
		assertEquals("김공공", savedUser.getUsername());
		assertEquals(UserSignUpType.EMAIL, savedUser.getSignupType());
		assertEquals(UserStatusType.ACTIVE, savedUser.getUserStatus());
	}

	// 이미 가입된 이메일의 경우
	@Test
	void testRegisterByRegisteredUser() {
		given(redisService.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);

		UserRegistrationRequest request = UserRegistrationRequest.builder()
			.email("example@gmail.com")
			.password("password")
			.nickname("example")
			.username("김공공")
			.build();
		BaseException exception = assertThrows(BaseException.class,
			() -> authService.register(request));

		assertEquals(BaseException.ALREADY_EXIST_EMAIL, exception);
	}

	// 중복된 닉네임인 경우
	@Test
	void testRegisterByDuplicatedNickname() {
		given(redisService.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(userRepository.existsByNickname(anyString()))
			.willReturn(true);

		UserRegistrationRequest request = UserRegistrationRequest.builder()
			.email("example@gmail.com")
			.password("password")
			.nickname("example")
			.username("김공공")
			.build();
		BaseException exception = assertThrows(BaseException.class,
			() -> authService.register(request));

		assertEquals(BaseException.ALREADY_EXIST_NICKNAME, exception);
	}

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
	void checkAuthKeyWhenCorrect() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(redisService.getAuthKeyValue(anyString()))
			.willReturn("010101");
		willDoNothing()
			.given(redisService).deleteAuthKeyValue(anyString());
		willDoNothing()
			.given(redisService).setAuthCompletedValue(anyString());

		boolean isCorrect = authService.checkAuthKey("example@gmail.com", "010101");

		assertTrue(isCorrect);
	}

	// Redis에 정보가 없거나 그 값과 다른 경우
	@Test
	void checkAuthKeyWhenNotCorrect() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(redisService.getAuthKeyValue(anyString()))
			.willReturn("111111");

		boolean isCorrect = authService.checkAuthKey("example@gmail.com", "010101");

		assertFalse(isCorrect);
	}

	@Test
	void checkAuthKeyByRegisteredUser() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.sendMailWithAuthKey("example@gmail.com"));

		assertEquals(BaseException.ALREADY_EXIST_EMAIL, exception);
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
