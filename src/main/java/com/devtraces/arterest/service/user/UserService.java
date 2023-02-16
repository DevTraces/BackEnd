package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.controller.user.dto.NicknameCheckResponse;
import com.devtraces.arterest.controller.user.dto.PasswordUpdateRequest;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.devtraces.arterest.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        User user = userRepository.findById(userId).orElseThrow(
                () -> BaseException.USER_NOT_FOUND
        );

        // 비밀번호와 입력한 비밀번호가 다른 경우
        if (!passwordEncoder.matches(beforePassword, user.getPassword())) {
            throw BaseException.WRONG_BEFORE_PASSWORD;
        }

        user.setPassword(passwordEncoder.encode(afterPassword));
        userRepository.save(user);
    }
}
