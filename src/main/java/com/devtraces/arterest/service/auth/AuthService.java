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
import com.devtraces.arterest.service.auth.util.AuthRedisUtil;
import com.devtraces.arterest.service.auth.util.TokenRedisUtil;
import com.devtraces.arterest.service.feed.application.FeedDeleteApplication;
import com.devtraces.arterest.service.mail.MailService;
import com.devtraces.arterest.service.notice.NoticeService;
import com.devtraces.arterest.service.reply.ReplyService;
import com.devtraces.arterest.service.rereply.RereplyService;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import com.devtraces.arterest.service.user.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

	private static final int AUTH_KEY_DIGIT = 6;
	private static final String SIGN_UP_KEY = "SIGN_UP_KEY:";
	private static final int SIGN_UP_KEY_VALID_MINUTE = 60 * 3; // 3???
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final S3Service s3Service;
	private final MailService mailService;
	private final ReplyService replyService;
	private final RereplyService rereplyService;
	private final AuthRedisUtil authRedisUtil;
	private final TokenRedisUtil tokenRedisUtil;
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

		// ????????? ????????? ??? ??? ???????????? null, false ??????
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

	private void validateRegistration(String email, String nickname) {
		if (authRedisUtil.notExistsAuthCompletedValue(email)) {
			throw BaseException.NOT_AUTHENTICATION_YET;
		}

		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		if (userRepository.existsByNickname(nickname)) {
			throw BaseException.ALREADY_EXIST_NICKNAME;
		}
	}

	@Transactional(readOnly = true)
	public void sendMailWithAuthKey(String email) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKey = generateAuthKey();
		sendAuthenticationEmail(email, authKey);
		authRedisUtil.setAuthKeyValue(email, authKey);
	}

	public String generateAuthKey() {
		Random random = new Random();
		StringBuilder resultNumber = new StringBuilder();

		for (int i = 0; i < AUTH_KEY_DIGIT; i++) {
			resultNumber.append(random.nextInt(9));	// 0~9 ????????? ?????? ?????? ??????
		}
		return resultNumber.toString();
	}

	private void sendAuthenticationEmail(String email, String authKey) {
		String subject = "ArtBubble ?????? ??????";
		String text = "<h2>????????? ????????????</h2>\n"
			+ "<p>ArtBubble??? ???????????? ?????? ???????????????.<br>????????? ??????????????? ??????????????? ????????? ??????????????? ???????????????.</p>\n"
			+ "<p style=\"background: #EFEFEF; font-size: 30px;padding: 10px\">" + authKey + "</p>";
		mailService.sendMail(email, subject, text);
	}

	public MailAuthKeyCheckResponse checkAuthKey(String email, String authKey) {
		if (userRepository.existsByEmail(email)) {
			throw BaseException.ALREADY_EXIST_EMAIL;
		}
		String authKeyInRedis = authRedisUtil.getAuthKeyValue(email);
		if (!authKey.equals(authKeyInRedis)) {
			return MailAuthKeyCheckResponse.from(null, false);
		}
		// ?????? ?????????????????? Redis ?????? ??????
		authRedisUtil.deleteAuthKeyValue(email);
		authRedisUtil.setAuthCompletedValue(email);

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

	private void validateLogin(User user, String passwordInput) {
		if (!passwordEncoder.matches(passwordInput, user.getPassword())) {
			throw BaseException.WRONG_EMAIL_OR_PASSWORD;
		}
	}

	@Transactional
	public void signOut(long userId, String accessToken) {
		tokenRedisUtil.deleteRefreshTokenBy(userId);

		// Access Token??? ??????????????? ??? ???????????? Redis??? ??????????????? ??????
		Date expiredDate = jwtProvider.getExpiredDate(accessToken);
		tokenRedisUtil.setAccessTokenBlackListValue(accessToken, userId, expiredDate);
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

		//????????? ??????
		followRepository.deleteAllByFollowingId(userId);
		followRepository.deleteAllByUser(user);

		//????????? ??????(?????? ????????? ?????? ??????, ?????????, ?????????, ?????????, ???????????? ??????)
		for(Feed feed : user.getFeedList()){
			feedDeleteApplication.deleteFeed(userId, feed.getId());
		}

		//????????? ??????
		bookmarkRepository.deleteAllByUser(user);

		//????????? ??????
		likeRepository.deleteAllByUserId(userId);

		//?????? ??????(?????? ?????? ?????? ????????? ??????)
		for(Reply reply : user.getReplyList()){
			Optional<Reply> replyOptional = replyRepository.findById(reply.getId());
			if(replyOptional.isPresent()) {
				replyService.deleteReply(
					userId, reply.getId()
				);
			}
		}

		//????????? ??????
		for(Rereply rereply : user.getRereplyList()){
			Optional<Rereply> rereplyOptional = rereplyRepository.findById(rereply.getId());
			if(rereplyOptional.isPresent()){
				rereplyService.deleteRereply(
					userId, rereply.getId()
				);
			}
		}

		// ????????? ????????? ?????? ??????
		noticeService.deleteNoticeWhenUserDeleted(user.getId());

		//?????? ??????
		userRepository.deleteById(userId);
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
}
