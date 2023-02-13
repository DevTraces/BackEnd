package com.devtraces.arterest.service.user.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserInfoFromKakaoDto {

    private long kakaoUserId;
    private String email;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private String description;
}
