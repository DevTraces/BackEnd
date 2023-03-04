package com.devtraces.arterest.model.converter;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedResponseConverter {

    Long getFeedId();

    String authorProfileImageUrl();
    String authorNickname();

    String getContent();
    String getImageUrls(); // "url1,rul2, ..."
    String getHashtags(); // "#감자,#potato, ..."

    Integer getNumberOfReply();

    LocalDateTime getCreatedAt();
    LocalDateTime getModifiedAt();

}
