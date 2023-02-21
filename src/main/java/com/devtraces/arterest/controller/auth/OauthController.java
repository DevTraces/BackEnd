package com.devtraces.arterest.controller.auth;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.auth.dto.request.OauthKakaoSignInRequest;
import com.devtraces.arterest.service.auth.OauthService;
import java.util.HashMap;

import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;
import static com.devtraces.arterest.controller.auth.AuthController.ACCESS_TOKEN_PREFIX;
import static com.devtraces.arterest.controller.auth.AuthController.SET_COOKIE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OauthController {

    private final OauthService oauthService;

    @PostMapping("/kakao/callback")
    public ResponseEntity<ApiSuccessResponse<?>> oauthKakaoSignIn(@RequestBody OauthKakaoSignInRequest request) {
        TokenWithNicknameDto dto =
                oauthService.oauthKakaoSignIn(request.getAccessTokenFromKakao());

        HashMap hashMap = new HashMap();
        hashMap.put(ACCESS_TOKEN_PREFIX, TOKEN_PREFIX + " " + dto.getAccessToken());
        hashMap.put("nickname", dto.getNickname());

        return ResponseEntity.ok()
            .header(SET_COOKIE, dto.getResponseCookie().toString())
            .body(ApiSuccessResponse.from(hashMap));
    }
}
