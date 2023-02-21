package com.devtraces.arterest.service.user.dto;

import com.devtraces.arterest.common.jwt.dto.TokenDto;
import lombok.*;
import org.springframework.http.ResponseCookie;

import static com.devtraces.arterest.common.jwt.JwtProperties.TOKEN_PREFIX;

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
