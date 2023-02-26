package com.devtraces.arterest.controller.notice.dto.response;

import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.user.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowNoticeDto extends NoticeListResponse {

    private Long noticeId;
    private String noticeType;
    private String senderNickname;
    private String senderProfileImageUrl;

    // isFollowing으로 하면 following으로 출력되기 때문에 is 중복 사용
    private boolean isIsFollowing;

    private String createdAt;

    public static FollowNoticeDto followNotice(Notice notice, boolean isFollowing) {
        User user = notice.getUser();

        return FollowNoticeDto.builder()
                .noticeId(notice.getId())
                .noticeType(notice.getNoticeType().toString())
                .senderNickname(user.getNickname())
                .senderProfileImageUrl(user.getProfileImageUrl())
                .isIsFollowing(isFollowing)
                .createdAt(notice.getCreatedAt().toString())
                .build();
    }
}
