package com.devtraces.arterest.controller.feed.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedResponseConverter {

    Long getFeedId();

    String getAuthorProfileImageUrl();
    String getAuthorNickname();

    String getContent();
    String getImageUrls(); // "url1,rul2, ..."
    String getHashtags(); // "#감자,#potato, ..."

    Integer getNumberOfReply();

    LocalDateTime getCreatedAt();
    LocalDateTime getModifiedAt();

}
