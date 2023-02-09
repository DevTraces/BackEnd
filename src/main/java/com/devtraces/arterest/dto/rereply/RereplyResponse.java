package com.devtraces.arterest.dto.rereply;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RereplyResponse {

    private Long rereplyId;
    private Long feedId;
    private Long replyId;

    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}
