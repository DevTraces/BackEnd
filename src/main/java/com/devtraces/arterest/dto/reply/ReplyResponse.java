package com.devtraces.arterest.dto.reply;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReplyResponse {

    private Long replyId;
    private Long feedId;

    private Long authorId;
    private String content;
    private String authorProfileImageUrl;

    private Long numberOfRereply;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}
