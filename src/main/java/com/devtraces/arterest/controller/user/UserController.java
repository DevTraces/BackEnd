package com.devtraces.arterest.controller.user;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.request.PasswordUpdateRequest;
import com.devtraces.arterest.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.annotation.Nullable;

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

    @GetMapping("/profile/{nickname}")
    public ApiSuccessResponse<?> getProfileByNickname(
            @AuthenticationPrincipal Long userId,
            @PathVariable String nickname
    ) {
        return ApiSuccessResponse.from(userService.getProfileByNickname(userId, nickname));
    }

    @PostMapping("/profile/{nickname}")
    public ApiSuccessResponse<?> updateProfile(
            @AuthenticationPrincipal Long userId,
            @PathVariable String nickname,
            @RequestParam @Nullable String updateUsername,
            @RequestParam @Nullable String updateNickname,
            @RequestParam @Nullable String updateDescription,
            @RequestParam @Nullable MultipartFile updateProfileImage

    ) {
        return ApiSuccessResponse.from(
                userService.updateProfile(
                        userId,
                        nickname,
                        updateUsername,
                        updateNickname,
                        updateDescription,
                        updateProfileImage
                )
        );
    }

}
