package com.devtraces.arterest.controller.user;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.EmailCheckRequest;
import com.devtraces.arterest.controller.user.dto.NicknameCheckRequest;
import com.devtraces.arterest.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/email/check")
    public ApiSuccessResponse<?> checkEmail(@RequestBody @Valid EmailCheckRequest request) {
        return ApiSuccessResponse.from(userService.checkEmail(request.getEmail()));
    }

    @GetMapping("/nickname/check")
    public ApiSuccessResponse<?> checkNickname(@RequestBody @Valid NicknameCheckRequest request) {
        return ApiSuccessResponse.from(userService.checkNickname(request.getNickname()));
    }
}
