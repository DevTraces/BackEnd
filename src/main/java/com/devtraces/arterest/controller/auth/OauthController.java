package com.devtraces.arterest.controller.auth;

import com.devtraces.arterest.common.response.ApiSuccessResponse;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.controller.auth.dto.request.OauthKakaoSignInRequest;
import com.devtraces.arterest.service.auth.OauthService;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OauthController {

    private final OauthService oauthService;

    @PostMapping("/kakao/callback")
    public ResponseEntity<ApiSuccessResponse<?>> oauthKakaoSignIn(
        @RequestBody OauthKakaoSignInRequest request,
        HttpServletResponse response
    ) {
        TokenWithNicknameDto dto =
                oauthService.oauthKakaoSignIn(request.getAccessTokenFromKakao());

        HashMap hashMap = new HashMap();
        hashMap.put("nickname", dto.getNickname());

        response.addHeader(HttpHeaders.SET_COOKIE, dto.getAcceesTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, dto.getRefreshTokenCookie().toString());

        return ResponseEntity.ok()
            .body(ApiSuccessResponse.from(hashMap));
    }
}
