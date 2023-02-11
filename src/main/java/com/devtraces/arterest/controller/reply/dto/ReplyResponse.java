package com.devtraces.arterest.controller.reply.dto;

import com.devtraces.arterest.domain.reply.Reply;
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
public class ReplyResponse {

    private Long replyId;
    private Long feedId;

    private String authorNickname;
    private String content;
    private String authorProfileImageUrl;

    private Integer numberOfRereply;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReplyResponse from(Reply reply){
        return ReplyResponse.builder()
            .replyId(reply.getId())
            .feedId(reply.getFeed().getId())
            .authorNickname(reply.getUser().getNickname())
            .content(reply.getContent())
            .authorProfileImageUrl(reply.getUser().getProfileImageUrl())
            .numberOfRereply(reply.getRereplyList() == null ? 0 : reply.getRereplyList().size())
            .createdAt(reply.getCreatedAt())
            .modifiedAt(reply.getModifiedAt())
            .build();
    }

}
