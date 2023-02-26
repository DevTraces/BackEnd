package com.devtraces.arterest.controller.notice.dto;

import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyNoticeDto extends NoticeListResponse {

    private Long noticeId;
    private String noticeType;
    private String senderNickname;
    private String senderProfileImageUrl;

    private Long feedId;
    private String feedFirstImageUrl;

    private Long replyId;
    private String replyContent;

    private String createdAt;

    public static ReplyNoticeDto convertToReplyNotice(Notice notice) {
        User user = notice.getUser();
        Feed feed = notice.getFeed();
        Reply reply = notice.getReply();

        return ReplyNoticeDto.builder()
                .noticeId(notice.getId())
                .noticeType(notice.getNoticeType().toString())
                .senderNickname(user.getNickname())
                .senderProfileImageUrl(user.getProfileImageUrl())
                .feedId(feed.getId())
                .feedFirstImageUrl(feed.getImageUrls().split(",")[0])
                .replyId(reply.getId())
                .replyContent(getShortenContent(reply.getContent()))
                .createdAt(notice.getCreatedAt().toString())
                .build();
    }
}
