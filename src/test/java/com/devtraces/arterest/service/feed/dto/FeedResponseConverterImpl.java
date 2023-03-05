package com.devtraces.arterest.service.feed.dto;

import com.devtraces.arterest.controller.feed.dto.response.FeedResponseConverter;
import java.time.LocalDateTime;

public class FeedResponseConverterImpl implements FeedResponseConverter {

    private Long feedId;

    private String authorProfileImageUrl;
    private String authorNickname;

    private String content;
    private String imageUrls;
    private String hashtags;

    private Integer numberOfReply;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public FeedResponseConverterImpl(Long feedId, String authorProfileImageUrl,
        String authorNickname,
        String content,
        String imageUrls,
        String hashtags,
        Integer numberOfReply,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {
        this.feedId = feedId;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.authorNickname = authorNickname;
        this.content = content;
        this.imageUrls = imageUrls;
        this.hashtags = hashtags;
        this.numberOfReply = numberOfReply;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }


    @Override
    public Long getFeedId() {
        return this.feedId;
    }

    @Override
    public String getAuthorProfileImageUrl() {
        return this.authorProfileImageUrl;
    }

    @Override
    public String getAuthorNickname() {
        return this.authorNickname;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public String getImageUrls() {
        return this.imageUrls;
    }

    @Override
    public String getHashtags() {
        return this.hashtags;
    }

    @Override
    public Integer getNumberOfReply() {
        return this.numberOfReply;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public LocalDateTime getModifiedAt() {
        return this.modifiedAt;
    }
}
