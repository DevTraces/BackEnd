package com.devtraces.arterest.service.reply;

import com.devtraces.arterest.controller.reply.dto.response.ReplyResponseConverter;
import java.time.LocalDateTime;

public class ReplyResponseConverterImpl implements ReplyResponseConverter {
    private Long replyId;

    private String content;

    private String authorNickname;
    private String authorProfileImageUrl;

    private int numberOfRereply;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public ReplyResponseConverterImpl(
        Long replyId,
        String content,
        String authorNickname,
        String authorProfileImageUrl,
        int numberOfRereply,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
    ) {
        this.replyId = replyId;
        this.content = content;
        this.authorNickname = authorNickname;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.numberOfRereply = numberOfRereply;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    @Override
    public Long getReplyId() {
        return this.replyId;
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
    public String getAuthorProfileImageUrl() {
        return this.authorProfileImageUrl;
    }

    @Override
    public Integer getNumberOfRereply() {
        return this.numberOfRereply;
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
