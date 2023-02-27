package com.devtraces.arterest.service.user;

import com.devtraces.arterest.controller.user.dto.response.*;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.service.s3.S3Service;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

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

    public ProfileByNicknameResponse getProfileByNickname(Long userId, String nickname) {
        User user = getUserById(userId);
        User targetUser = userRepository.findByNickname(nickname).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );

        int totalFeedNumber = feedRepository.countAllByUserId(targetUser.getId());

        Integer followerNumber = followRepository.countAllByFollowingId(targetUser.getId());
        Integer followingNumber = targetUser.getFollowList().size();
        boolean isFollowing = followRepository.isFollowing(
                user.getId(), targetUser.getId()) == 0 ? false : true;

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

    private User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> BaseException.USER_NOT_FOUND
        );
        return user;
    }
}
