package com.devtraces.arterest.service.rereply;

import com.devtraces.arterest.controller.rereply.dto.response.RereplyResponseConverter;
import java.time.LocalDateTime;

public class RereplyResponseConverterImpl implements RereplyResponseConverter {

    private Long rereplyId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;

    public RereplyResponseConverterImpl(
        Long rereplyId,
        String authorNickname,
        String authorProfileImageUrl,
        String content,
        LocalDateTime createAt,
        LocalDateTime modifiedAt
    ) {
        this.rereplyId = rereplyId;
        this.authorNickname = authorNickname;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.content = content;
        this.createAt = createAt;
        this.modifiedAt = modifiedAt;
    }

    @Override
    public Long getRereplyId() {
        return this.rereplyId;
    }

    @Override
    public String getAuthorNickname() {
        return this.authorNickname;
    }

    @Override
    public String getAuthorProfileImageUrl() {
        return this.authorProfileImageUrl;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return this.createAt;
    }

    @Override
    public LocalDateTime getModifiedAt() {
        return this.modifiedAt;
    }
}
