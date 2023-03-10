package com.devtraces.arterest.controller.reply.dto.response;

import java.time.LocalDateTime;

public interface ReplyResponseConverter {

    Long getReplyId();

    String getAuthorNickname();
    String getContent();
    String getAuthorProfileImageUrl();

    Integer getNumberOfRereply();

    LocalDateTime getCreatedAt();
    LocalDateTime getModifiedAt();

}
