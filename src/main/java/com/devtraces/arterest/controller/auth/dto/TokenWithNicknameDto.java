package com.devtraces.arterest.controller.auth.dto;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import lombok.*;
import org.springframework.http.ResponseCookie;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TokenWithNicknameDto {

    private String nickname;
    private String accessToken;
    private ResponseCookie responseCookie;

    public static TokenWithNicknameDto from(String nickname, TokenDto tokenDto) {
        return TokenWithNicknameDto.builder()
                .nickname(nickname)
                .accessToken(tokenDto.getAccessToken())
                .responseCookie(tokenDto.getResponseCookie())
                .build();
    }
}
