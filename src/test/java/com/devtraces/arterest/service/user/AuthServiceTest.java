package com.devtraces.arterest.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.service.auth.AuthService;
import com.devtraces.arterest.service.mail.MailService;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.service.auth.util.AuthRedisUtil;
import com.devtraces.arterest.controller.auth.dto.request.UserRegistrationRequest;
import com.devtraces.arterest.controller.auth.dto.response.UserRegistrationResponse;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.Optional;

import com.devtraces.arterest.service.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private MailService mailService;
	@Mock
	private AuthRedisUtil authRedisUtil;
	@Mock
	private UserRepository userRepository;
	@Mock
	private S3Service s3Service;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원가입 성공")
	void testRegister() {
		String email = "example@gmail.com";
		String password = "password";
		String nickname = "example";
		String username = "김공공";
		MockMultipartFile profileImage =
				new MockMultipartFile("profileImage", "profileImage".getBytes());
		String description = "description";
		String encodingPassword = "encodingPassword";
		String profileImageUrl = "profileImageUrl";
		User mockUser = User.builder()
				.email(email)
				.password(encodingPassword)
				.nickname(nickname)
				.username(username)
				.signupType(UserSignUpType.EMAIL)
				.userStatus(UserStatusType.ACTIVE)
				.build();

		given(authRedisUtil.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(userRepository.existsByNickname(anyString()))
			.willReturn(false);
		given(s3Service.uploadImage(profileImage))
				.willReturn(profileImageUrl);
		given(passwordEncoder.encode(anyString()))
			.willReturn(encodingPassword);
		given(userRepository.save(any()))
			.willReturn(mockUser);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		authService.register(
				email, password, username, nickname, profileImage, description
		);

		verify(userRepository, times(1)).save(captor.capture());
		User savedUser = captor.getValue();
		assertEquals(email, savedUser.getEmail());
		assertEquals(encodingPassword, savedUser.getPassword());
		assertEquals(nickname, savedUser.getNickname());
		assertEquals(username, savedUser.getUsername());
		assertEquals(UserSignUpType.EMAIL, savedUser.getSignupType());
		assertEquals(UserStatusType.ACTIVE, savedUser.getUserStatus());
	}

	@Test
	@DisplayName("회원가입 실패 - 이미 가입된 이메일인 경우")
	void testRegisterByRegisteredUser() {
		String email = "example@gmail.com";
		String password = "password";
		String nickname = "example";
		String username = "김공공";
		MockMultipartFile profileImage =
				new MockMultipartFile("profileImage", "profileImage".getBytes());
		String description = "description";

		given(authRedisUtil.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.register(email, password, username, nickname, profileImage, description));

		assertEquals(BaseException.ALREADY_EXIST_EMAIL, exception);
	}

	@Test
	@DisplayName("회원가입 실패 - 중복된 닉네임인 경우")
	void testRegisterByDuplicatedNickname() {
		String email = "example@gmail.com";
		String password = "password";
		String nickname = "example";
		String username = "김공공";
		MockMultipartFile profileImage =
				new MockMultipartFile("profileImage", "profileImage".getBytes());
		String description = "description";

		given(authRedisUtil.notExistsAuthCompletedValue(anyString()))
			.willReturn(false);
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(userRepository.existsByNickname(anyString()))
			.willReturn(true);

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.register(email, password, username, nickname, profileImage, description));

		assertEquals(BaseException.ALREADY_EXIST_NICKNAME, exception);
	}

	@Test
	void testSendMailWithAuthKey() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		willDoNothing()
			.given(mailService).sendMail(anyString(), anyString(), anyString());
		willDoNothing()
			.given(authRedisUtil).setAuthKeyValue(anyString(), anyString());

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
		given(authRedisUtil.getAuthKeyValue(anyString()))
			.willReturn("010101");
		willDoNothing()
			.given(authRedisUtil).deleteAuthKeyValue(anyString());
		willDoNothing()
			.given(authRedisUtil).setAuthCompletedValue(anyString());

		boolean isCorrect = authService.checkAuthKey("example@gmail.com", "010101");

		assertTrue(isCorrect);
	}

	// Redis에 정보가 없거나 그 값과 다른 경우
	@Test
	void checkAuthKeyWhenNotCorrect() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(authRedisUtil.getAuthKeyValue(anyString()))
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
			.accessTokenCookie(ResponseCookie.from("accessToken", "access-token").build())
			.refreshTokenCookie(ResponseCookie.from("refreshToken", "refresh-token").build())
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

	@Test
	void testCheckPasswordByCorrectPassword() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);

		boolean isCorrect = authService.checkPassword(1L, "password");

		assertTrue(isCorrect);
	}

	@Test
	void testCheckPasswordByWrongPassword() {
		User mockUser = User.builder()
			.id(1L)
			.email("email@gmail.com")
			.password("encodingPassword")
			.build();
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		boolean isCorrect = authService.checkPassword(1L, "wrong-password");

		assertFalse(isCorrect);
	}

	// Optional이므로 null인 경우에 대한 예외를 처리했지만,
	// userId는 인증인가를 하여 얻은 값이므로 이 예외가 발생할 가능성은 0%다.
	@Test
	void testCheckPasswordByWrongUserId() {
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
			() -> authService.checkPassword(0L, "password"));

		assertEquals(BaseException.USER_NOT_FOUND, exception);
	}
}
