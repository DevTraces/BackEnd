package com.devtraces.arterest.controller.notice.dto;


import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.notice.Notice;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeNoticeDto extends NoticeListResponse {

    private Long noticeId;
    private String noticeType;
    private String senderNickname;
    private String senderProfileImageUrl;

    private Long feedId;
    private String feedFirstImageUrl;
    private String feedContent;

    private String createdAt;

    public static LikeNoticeDto likeNotice(Notice notice) {
        Feed feed = notice.getFeed();

        return LikeNoticeDto.builder()
                .noticeId(notice.getId())
                .noticeType(notice.getNoticeType().toString())
                .senderNickname(notice.getUser().getNickname())
                .senderProfileImageUrl(notice.getUser().getProfileImageUrl())
                .feedId(feed.getId())
                .feedFirstImageUrl(feed.getImageUrls().split(",")[0]) // 피드 첫번째 이미지 가져오기
                .feedContent(getShortenContent(feed.getContent()))
                .createdAt(notice.getCreatedAt().toString())
                .build();
    }
}
