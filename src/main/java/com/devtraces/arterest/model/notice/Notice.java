package com.devtraces.arterest.model.notice;

import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.common.type.NoticeTarget;
import com.devtraces.arterest.common.type.NoticeType;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.user.User;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@EntityListeners(value = {AuditingEntityListener.class})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notice extends BaseEntity {

    @Id
    @Column(name = "notice_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long noticeOwnerId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 알림을 발생시킨 유저

    @ManyToOne
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @ManyToOne
    @JoinColumn(name = "rereply_id")
    private Rereply rereply;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Enumerated(EnumType.STRING)
    private NoticeTarget noticeTarget;

    @CreatedDate
    private LocalDateTime createdAt;
}
