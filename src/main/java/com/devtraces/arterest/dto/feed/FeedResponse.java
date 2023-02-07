package com.devtraces.arterest.dto.feed;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedResponse {

    private Long feedId;

    private Long authorId;
    private String authorProfileImageLink;
    private String authorNickname;

    private String content;
    private List<String> imageLinks;
    private List<String> hashtags; // ["#감자", "#potato", "#이얍이얍"]

    private Integer numberOfLike;
    private Integer numberOfReply;
    private boolean likeBefore; // 트루이면 좋아요 눌렀던 게시물인 것.

    private LocalDateTime createdAt; // 프런트엔드 측에서는 "2023-02-07T09:59:23.653281"라는 문자열 받음.
    private LocalDateTime modifiedAt;

}
