package com.devtraces.arterest.controller.auth.dto;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import javax.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TokenWithNicknameDto {

    private String nickname;
    private String accessToken;
    private ResponseCookie cookie;

    public static TokenWithNicknameDto from(String nickname, TokenDto tokenDto) {
        return TokenWithNicknameDto.builder()
                .nickname(nickname)
                .accessToken(tokenDto.getAccessToken())
                .cookie(tokenDto.getCookie())
                .build();
    }
}
