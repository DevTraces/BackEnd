package com.devtraces.arterest.controller.rereply.dto.response;

import java.time.LocalDateTime;

public interface RereplyResponseConverter {

    Long getRereplyId();

    // feedId는 FE에서 오기 때문에 필요 없다.

    // replyId는 FE에서 오기 때문에 필요 없다.

    String getAuthorNickname();
    String getAuthorProfileImageUrl();

    String getContent();

    LocalDateTime getCreatedAt();
    LocalDateTime getModifiedAt();

}
