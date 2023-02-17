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

    @PostMapping("/kakao/callback")
    public ResponseEntity<ApiSuccessResponse<?>> oauthKakaoSignIn(@RequestBody OauthKakaoSignInRequest request) {
        TokenDto tokenDto = oauthService.oauthKakaoSignIn(request.getAccessToken());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER,
                TOKEN_PREFIX + " " + tokenDto.getAccessToken());
        httpHeaders.add("X-REFRESH-TOKEN", tokenDto.getRefreshToken());

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(ApiSuccessResponse.NO_DATA_RESPONSE);
    }
}
