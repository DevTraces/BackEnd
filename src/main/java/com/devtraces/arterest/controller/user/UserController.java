package com.devtraces.arterest.controller.user;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.EmailCheckRequest;
import com.devtraces.arterest.controller.user.dto.NicknameCheckRequest;
import com.devtraces.arterest.controller.user.dto.PasswordUpdateRequest;
import com.devtraces.arterest.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/email/check")
    public ApiSuccessResponse<?> checkEmail(@RequestParam String email) {
        return ApiSuccessResponse.from(userService.checkEmail(email));
    }

    @GetMapping("/nickname/check")
    public ApiSuccessResponse<?> checkNickname(@RequestParam String nickname) {
        return ApiSuccessResponse.from(userService.checkNickname(nickname));
    }

    @PatchMapping("/password")
    public ApiSuccessResponse<?> updatePassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody PasswordUpdateRequest request
    ) {
        userService.updatePassword(
                userId, request.getBeforePassword(), request.getAfterPassword()
        );
        return ApiSuccessResponse.NO_DATA_RESPONSE;
    }
}
