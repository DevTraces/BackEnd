package com.devtraces.arterest.controller.rereply.dto.response;

import com.devtraces.arterest.model.rereply.Rereply;
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

    public static RereplyResponse fromConverter(
        RereplyResponseConverter rereplyResponseConverter, Long feedId, Long replyId
    ){
        return RereplyResponse.builder()
            .rereplyId(rereplyResponseConverter.getRereplyId())
            .feedId(feedId)
            .replyId(replyId)
            .authorNickname(rereplyResponseConverter.getAuthorNickname())
            .authorProfileImageUrl(rereplyResponseConverter.getAuthorProfileImageUrl())
            .content(rereplyResponseConverter.getContent())
            .createdAt(rereplyResponseConverter.getCreatedAt())
            .modifiedAt(rereplyResponseConverter.getCreatedAt())
            .build();
    }

    public static RereplyResponse from(Rereply rereply, Long feedId){
        return RereplyResponse.builder()
            .rereplyId(rereply.getId())
            .feedId(feedId)
            .replyId(rereply.getReply().getId())
            .authorNickname(rereply.getUser().getNickname())
            .authorProfileImageUrl(rereply.getUser().getProfileImageUrl())
            .content(rereply.getContent())
            .createdAt(rereply.getCreatedAt())
            .modifiedAt(rereply.getModifiedAt())
            .build();
    }

}
