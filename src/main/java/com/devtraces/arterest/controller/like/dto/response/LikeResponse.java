package com.devtraces.arterest.controller.like.dto.response;

import com.devtraces.arterest.common.type.UserSignUpType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeResponse {

    private Long userId;
    private String profileImageLink;
    private String userName;
    private String nickname;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private UserSignUpType signupType;

}
