package com.devtraces.arterest.service.user;

import com.devtraces.arterest.controller.user.dto.response.*;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.service.mail.MailService;
import com.devtraces.arterest.service.s3.S3Service;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

import static com.devtraces.arterest.common.type.UserSignUpType.KAKAO_TALK;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int AUTH_KEY_DIGIT = 6;
    private static final String PASSWORD_AUTH_KEY_PREFIX = "PASSWORD_AUTH_EMAIL:";
    private static final int AUTH_KEY_VALID_MINUTE = 10;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final S3Service s3Service;
    private final RedisTemplate<String, String> redisTemplate;

    public EmailCheckResponse checkEmail(String email) {

        // 이메일이 중복되지 않는 경우 duplicatedEmail : false 전송
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return EmailCheckResponse.from(false);
        }

        // 이메일이 중복되는 경우에는 duplicatedEmail : true 전송
        return EmailCheckResponse.from(true);
    }

    public NicknameCheckResponse checkNickname(String nickname) {

        // 닉네임이 중복되지 않는 경우 duplicatedNickname : false 전송
        Optional<User> optionalUser = userRepository.findByNickname(nickname);
        if (!optionalUser.isPresent()) {
            return NicknameCheckResponse.from(false);
        }

        // 닉네임이 중복되는 경우에는 duplicatedNickname : true 전송
        return NicknameCheckResponse.from(true);
    }

    public void updatePassword(
            Long userId, String beforePassword, String afterPassword
    ) {
        User user = getUserById(userId);

        // 비밀번호와 입력한 비밀번호가 다른 경우
        if (!passwordEncoder.matches(beforePassword, user.getPassword())) {
            throw BaseException.WRONG_BEFORE_PASSWORD;
        }

        user.setPassword(passwordEncoder.encode(afterPassword));
        userRepository.save(user);
    }

    public void sendMailWithAuthkeyForNewPassword(
            Long userId, String email
    ) {
        User user = getUserById(userId);

        // 이메일로 회원가입한 사람만 이메일 인증 가능
        // 카카오 소셜 로그인은 이메일 정보 없을 수도 있음
        if (user.getSignupType().equals(KAKAO_TALK)) {
            throw BaseException.UPDATE_PASSWORD_NOT_ALLOWED_FOR_KAKAO_USER;
        }

        // 본인 이메일에 대해서만 인증 가능
        checkInputEmailAndUserEmail(email, user.getEmail());

        String authKey = generateAuthKey();
        sendAuthenticationEmail(email, authKey);
        setAuthKeyInRedis(email, authKey);
    }

    public CheckAuthkeyAndSaveNewPasswordResponse checkAuthKeyAndSaveNewPassword(
            Long userId, String email, String authKey, String newPassword
    ) {
        User user = getUserById(userId);
        checkInputEmailAndUserEmail(email, user.getEmail());

        String authKeyOfEmail =
                redisTemplate.opsForValue().get(PASSWORD_AUTH_KEY_PREFIX + email);
        if (!authKey.equals(authKeyOfEmail)) {
            return CheckAuthkeyAndSaveNewPasswordResponse.from(false);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(PASSWORD_AUTH_KEY_PREFIX + email);

        return CheckAuthkeyAndSaveNewPasswordResponse.from(true);
    }

    public ProfileByNicknameResponse getProfileByNickname(Long userId, String nickname) {
        User user = getUserById(userId);
        User targetUser = userRepository.findByNickname(nickname).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );

        int totalFeedNumber = feedRepository.countAllByUserId(targetUser.getId());

        Integer followerNumber = followRepository.countAllByFollowingId(targetUser.getId());
        Integer followingNumber = targetUser.getFollowList().size();
        boolean isFollowing = followRepository.isFollowing(
                user.getId(), targetUser.getId()) != 0;

        return ProfileByNicknameResponse.from(
                targetUser, totalFeedNumber,
                followerNumber, followingNumber, isFollowing
        );
    }

    public UpdateProfileResponse updateProfile(
            Long userId, String nickname,
            String updateUsername, String updateNickname,
            String updateDescription
            ) {
        User user = getUserById(userId);

        // 본인의 프로필 정보만 수정가능
        if (!user.getNickname().equals(nickname)) { throw BaseException.FORBIDDEN; }

        if (updateUsername != null) { user.setUsername(updateUsername); }

        if (updateNickname != null) { user.setNickname(updateNickname); }

        if (updateDescription != null) { user.setDescription(updateDescription); }

        return UpdateProfileResponse.from(userRepository.save(user));
    }

    public UpdateProfileImageResponse updateProfileImage(
            Long userId, String nickname, MultipartFile profileImage
    ) {
        User user = getUserById(userId);

        if (!user.getNickname().equals(nickname)) { throw BaseException.FORBIDDEN; }

        user.setProfileImageUrl(s3Service.uploadImage(profileImage));

        return UpdateProfileImageResponse.from(userRepository.save(user));
    }

    // 프로필 이미지 삭제는 사용자의 프로필 이미지를 null로 수정하는 것으로 진행
    public void deleteProfileImage(Long userId, String nickname) {
        User user = getUserById(userId);

        if (!user.getNickname().equals(nickname)) { throw BaseException.FORBIDDEN; }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> BaseException.USER_NOT_FOUND);
    }

    private String generateAuthKey() {
        Random random = new Random();
        StringBuilder resultNumber = new StringBuilder();

        for (int i = 0; i < AUTH_KEY_DIGIT; i++) {
            resultNumber.append(random.nextInt(9));	// 0~9 사이의 랜덤 숫자 생성
        }
        return resultNumber.toString();
    }

    private void sendAuthenticationEmail(String email, String authKey) {
        String subject = "ArtBubble 인증 코드";
        String text = "<h2>새 비밀번호 설정을 위한 이메일 인증코드</h2>\n"
                + "<p>새 비밀번호 설정을 위한 인증코드입니다. <br>아래의 인증코드를 입력하시면 새 비밀번호 설정이 완료됩니다.</p>\n"
                + "<p style=\"background: #EFEFEF; font-size: 30px;padding: 10px\">" + authKey + "</p>";

        mailService.sendMail(email, subject, text);
    }

    private static void checkInputEmailAndUserEmail(
            String email, String userEmail
    ) {
        if (!userEmail.equals(email)) {
            throw BaseException.INPUT_EMAIL_AND_USER_EMAIL_MISMATCH;
        }
    }

    private void setAuthKeyInRedis(String email, String authKey) {
        redisTemplate
                .opsForValue()
                .set(
                        PASSWORD_AUTH_KEY_PREFIX + email,
                        authKey,
                        Duration.ofMinutes(AUTH_KEY_VALID_MINUTE)
                );
    }
}
