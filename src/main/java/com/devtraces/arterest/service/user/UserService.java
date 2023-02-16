package com.devtraces.arterest.service.user;

import com.devtraces.arterest.controller.user.dto.EmailCheckResponse;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public EmailCheckResponse checkEmail(String email) {

        // 이메일이 중복되지 않는 경우 duplicatedEmail : false 전송
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return EmailCheckResponse.from(false);
        }

        // 이메일 중복되는 경우에는 duplicatedEmail : true 전송
        return EmailCheckResponse.from(true);
    }
}
