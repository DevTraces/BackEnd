package com.devtraces.arterest.service.auth;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.controller.auth.dto.response.MailAuthKeyCheckResponse;
import com.devtraces.arterest.controller.auth.dto.response.UserRegistrationResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.feed.application.FeedDeleteApplication;
import com.devtraces.arterest.service.mail.MailService;
import com.devtraces.arterest.service.notice.NoticeService;
import com.devtraces.arterest.service.reply.ReplyService;
import com.devtraces.arterest.service.rereply.RereplyService;
import com.devtraces.arterest.service.s3.S3Service;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

import com.devtraces.arterest.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.devtraces.arterest.common.jwt.JwtProvider.REFRESH_TOKEN_SUBJECT_PREFIX;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

	private static final int AUTH_KEY_DIGIT = 6;
	private static final String SIGN_UP_KEY = "SIGN_UP_KEY:";
	private static final String AUTH_KEY_PREFIX = "AK:";
	private static final String ACCESS_TOKEN_BLACK_LIST_PREFIX = "AT-BL:";
	private static final String AUTH_COMPLETED_PREFIX = "AC:";
	private static final int AUTH_KEY_VALID_MINUTE = 60 * 3;
	private static final int SIGN_UP_KEY_VALID_MINUTE = 60 * 3; // 3분
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final S3Service s3Service;
	private final MailService mailService;
	private final ReplyService replyService;
	private final RereplyService rereplyService;
	private final UserRepository userRepository;
	private final FeedDeleteApplication feedDeleteApplication;
	private final FollowRepository followRepository;
	private final NoticeService noticeService;
	private final BookmarkRepository bookmarkRepository;
	private final LikeRepository likeRepository;
	private final ReplyRepository replyRepository;
	private final RereplyRepository rereplyRepository;
	private final RedisService redisService;

	@Transactional
	public UserRegistrationResponse register(
			String email, String password, String username,
			String nickname, MultipartFile profileImage,
			String description, String signUpKey
	) {
		String emailBySignUpKey =
				redisService.getData(SIGN_UP_KEY + signUpKey);

		// 이메일 인증이 안 된 사용자는 null, false 반환
		if (!email.equals(emailBySignUpKey)) {
			return UserRegistrationResponse.from(
					User.builder().build(), false
			);
		}

		validateRegistration(email, nickname);

		String profileImageUrl = null;
		if (profileImage != null) {
			profileImageUrl = s3Service.uploadImage(profileImage);
		}

		User savedUser = userRepository.save(User.builder()
				.email(email)
				.password(passwordEncoder.encode(password))
				.username(username)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.description(description)
				.signupType(UserSignUpType.EMAIL)
				.userStatus(UserStatusType.ACTIVE)
				.build());

		return UserRegistrationResponse.from(savedUser, true);
	}

	@Transactional(readOnly = true)
	public void sendMailWithAuthKey(String email) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKey = generateAuthKey();
		sendAuthenticationEmail(email, authKey);
		redisService.setDataExpire(
				AUTH_KEY_PREFIX + email,
				authKey,
				AUTH_KEY_VALID_MINUTE
		);
	}

	public String generateAuthKey() {
		Random random = new Random();
		StringBuilder resultNumber = new StringBuilder();

		for (int i = 0; i < AUTH_KEY_DIGIT; i++) {
			resultNumber.append(random.nextInt(9));	// 0~9 사이의 랜덤 숫자 생성
		}
		return resultNumber.toString();
	}

	public MailAuthKeyCheckResponse checkAuthKey(String email, String authKey) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKeyInRedis =
				redisService.getData(AUTH_KEY_PREFIX + email);
		if (!authKey.equals(authKeyInRedis)) {
			return MailAuthKeyCheckResponse.from(null, false);
		}
		// 인증 완료했으므로 Redis 정보 변경
		redisService.deleteData(AUTH_KEY_PREFIX + email);

		// 인증 완료한 이메일을 redis에 저장
		redisService.setData(AUTH_COMPLETED_PREFIX + email, "O");

		String signUpKey = generateRandomSignUpKey();
		redisService.setDataExpire(
				SIGN_UP_KEY + signUpKey, email, SIGN_UP_KEY_VALID_MINUTE
		);

		return MailAuthKeyCheckResponse.from(signUpKey, true);
	}

	@Transactional(readOnly = true)
	public TokenWithNicknameDto signInAndGenerateJwtToken(String email, String password) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BaseException.WRONG_EMAIL_OR_PASSWORD);
		validateLogin(user, password);

		TokenDto tokenDto =
				jwtProvider.generateAccessTokenAndRefreshToken(user.getId());

		return TokenWithNicknameDto.from(user.getNickname(), tokenDto);
	}
	@Transactional
	public void signOut(long userId, String accessToken) {
		redisService.deleteData(REFRESH_TOKEN_SUBJECT_PREFIX + userId);

		// Access Token을 무효화시킬 수 없으므로 Redis에 블랙리스트 작성
		Date expiredDate = jwtProvider.getExpiredDate(accessToken);

		// 블랙리스트에 해당 토큰이 등록되었는지 확인하기 위해 accessToken을 key로 지정
		setAccessTokenBlackListValue(userId, accessToken, expiredDate);
	}

	public boolean checkPassword(long userId, String password) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);
		return passwordEncoder.matches(password, user.getPassword());
	}

	@Transactional
	public void deleteUser(long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);

		//팔로우 삭제
		followRepository.deleteAllByFollowingId(userId);
		followRepository.deleteAllByUser(user);

		//게시물 삭제(해당 게시물 관련 댓글, 대댓글, 좋아요, 북마크, 해시태그 삭제)
		for(Feed feed : user.getFeedList()){
			feedDeleteApplication.deleteFeed(userId, feed.getId());
		}

		//북마크 삭제
		bookmarkRepository.deleteAllByUser(user);

		//좋아요 삭제
		likeRepository.deleteAllByUserId(userId);

		//댓글 삭제(해당 댓글 관련 대댓글 삭제)
		for(Reply reply : user.getReplyList()){
			Optional<Reply> replyOptional = replyRepository.findById(reply.getId());
			if(replyOptional.isPresent()) {
				replyService.deleteReply(
					userId, reply.getId()
				);
			}
		}

		//대댓글 삭제
		for(Rereply rereply : user.getRereplyList()){
			Optional<Rereply> rereplyOptional = rereplyRepository.findById(rereply.getId());
			if(rereplyOptional.isPresent()){
				rereplyService.deleteRereply(
					userId, rereply.getId()
				);
			}
		}

		// 유저와 관련된 알림 삭제
		noticeService.deleteNoticeWhenUserDeleted(user.getId());

		//유저 삭제
		userRepository.deleteById(userId);
	}

	private void setAccessTokenBlackListValue(long userId, String accessToken, Date expiredDate) {
		Date now = new Date();
		long expirationSeconds = (expiredDate.getTime() - now.getTime()) / 1000;

		if (expirationSeconds > 0) {
			redisService.setDataExpire(
					ACCESS_TOKEN_BLACK_LIST_PREFIX + accessToken,
					String.valueOf(userId),
					expirationSeconds
			);
		}
	}

	private String generateRandomSignUpKey() {
		String randomSignUpPasswordKey = "";
		do {
			for (int i = 0; i < 6; i++) {
				randomSignUpPasswordKey += (char) ((int) (Math.random() * 25) + 97);
			}
		} while (redisService.existKey(randomSignUpPasswordKey));

		return randomSignUpPasswordKey;
	}

	private void sendAuthenticationEmail(String email, String authKey) {
		String subject = "ArtBubble 인증 코드";
		String text = "<h2>이메일 인증코드</h2>\n"
				+ "<p>ArtBubble에 가입하신 것을 환영합니다.<br>아래의 인증코드를 입력하시면 가입이 정상적으로 완료됩니다.</p>\n"
				+ "<p style=\"background: #EFEFEF; font-size: 30px;padding: 10px\">" + authKey + "</p>";
		mailService.sendMail(email, subject, text);
	}

	private void validateRegistration(String email, String nickname) {
		if (redisService.getData(AUTH_COMPLETED_PREFIX + email) == null) {
			throw BaseException.NOT_AUTHENTICATION_YET;
		}
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		if (userRepository.existsByNickname(nickname)) {
			throw BaseException.ALREADY_EXIST_NICKNAME;
		}
	}

	private void validateLogin(User user, String passwordInput) {
		if (!passwordEncoder.matches(passwordInput, user.getPassword())) {
			throw BaseException.WRONG_EMAIL_OR_PASSWORD;
		}
	}
}
