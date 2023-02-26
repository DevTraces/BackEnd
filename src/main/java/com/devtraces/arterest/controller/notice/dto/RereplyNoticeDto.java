package com.devtraces.arterest.controller.notice.dto;

import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RereplyNoticeDto extends NoticeListResponse {

    private Long noticeId;
    private String noticeType;
    private String senderNickname;
    private String senderProfileImageUrl;

    private Long feedId;
    private String feedFirstImageUrl;

    private Long replyId;

    private Long rereplyId;
    private String rereplyContent;

    private String noticeTarget;

    private String createdAt;

    public static RereplyNoticeDto convertToRereplyNotice(Notice notice) {
        User user = notice.getUser();
        Feed feed = notice.getFeed();
        Reply reply = notice.getReply();
        Rereply rereply = notice.getRereply();

        return RereplyNoticeDto.builder()
                .noticeId(notice.getId())
                .noticeType(notice.getNoticeType().toString())
                .senderNickname(user.getNickname())
                .senderProfileImageUrl(user.getProfileImageUrl())
                .feedId(feed.getId())
                .feedFirstImageUrl(feed.getImageUrls().split(",")[0])
                .replyId(reply.getId())
                .rereplyId(rereply.getId())
                .rereplyContent(getShortenContent(rereply.getContent()))
                .noticeTarget(notice.getNoticeTarget().toString())
                .createdAt(notice.getCreatedAt().toString())
                .build();
    }
}
