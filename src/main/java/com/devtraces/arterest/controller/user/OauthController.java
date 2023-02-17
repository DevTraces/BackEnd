package com.devtraces.arterest.controller.user;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.user.dto.OauthKakaoSignInRequest;
import com.devtraces.arterest.service.user.AuthService;
import com.devtraces.arterest.service.user.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.devtraces.arterest.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OauthController {

    private final OauthService oauthService;

    public static final String SET_COOKIE = "Set-Cookie";

    @PostMapping("/kakao/callback")
    public ResponseEntity<?> oauthKakaoSignIn(@RequestBody OauthKakaoSignInRequest request) {
        TokenDto tokenDto = oauthService.oauthKakaoSignIn(request.getAccessToken());

        return ResponseEntity.ok()
            .header(SET_COOKIE, tokenDto.getResponseCookie().toString())
            .body(tokenDto.getAccessToken());
    }
}
