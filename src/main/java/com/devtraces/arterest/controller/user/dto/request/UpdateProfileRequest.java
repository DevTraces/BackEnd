package com.devtraces.arterest.controller.user.dto.request;

import lombok.*;
import reactor.util.annotation.Nullable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProfileRequest {

    @Nullable
    private String username;

    @Nullable
    private String nickname;

    @Nullable
    private String description;
}
