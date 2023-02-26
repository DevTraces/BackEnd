package com.devtraces.arterest.service.notice;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.type.NoticeTarget;
import com.devtraces.arterest.common.type.NoticeType;
import com.devtraces.arterest.controller.notice.dto.NumberOfNoticeResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.notice.NoticeRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;

    public void createLikeNotice(
            Long sendUserId, Long feedId
    ) {
        Feed feed = getFeed(feedId);

        noticeRepository.save(
                Notice.builder()
                        .noticeOwnerId(feed.getUser().getId()) // 좋아요 누른 피드의 주인
                        .user(getUser(sendUserId)) // 좋아요 누른 사용자
                        .feed(feed)
                        .noticeType(NoticeType.LIKE)
                        .build()
        );
    }

    public void createFollowNotice(String nickname, Long sendUserId) {
        User ownerUser = userRepository.findByNickname(nickname).orElseThrow(
                () -> BaseException.USER_NOT_FOUND
        );

        noticeRepository.save(
                Notice.builder()
                        .noticeOwnerId(ownerUser.getId()) // 팔로우 당한 사람
                        .user(getUser(sendUserId)) // 팔로우 한 사람
                        .noticeType(NoticeType.FOLLOW)
                        .build()
        );
    }

    public void createReplyNotice(
            Long sendUserId, Long feedId, Long replyId
    ) {
        Feed feed = getFeed(feedId);

        noticeRepository.save(
                Notice.builder()
                        .noticeOwnerId(feed.getUser().getId()) // 댓글 단 피드의 주인
                        .user(getUser(sendUserId)) // 댓글 단 사람
                        .feed(feed)
                        .reply(getReply(replyId))
                        .noticeType(NoticeType.REPLY)
                        .build()
        );
    }

    public void createReReplyNotice(
            Long sendUserId, Long feedId,
            Long replyId, Long reReplyId
    ) {
        User sendUser = getUser(sendUserId);
        Feed feed = getFeed(feedId);
        Reply reply = getReply(replyId);
        Rereply reReply = getReReply(reReplyId);

        // feed 주인에게 알림 저장
        saveNoticeForFeedOwner(sendUser, feed, reply, reReply);

        // 댓글 주인에게 알림 저장
        saveNoticeForReplyOwner(sendUser, feed, reply, reReply);
    }

    public void saveNoticeForFeedOwner(
            User sendUser, Feed feed, Reply reply, Rereply reReply
    ) {
        noticeRepository.save(
                buildReReplyNotice(
                        feed.getUser().getId(), // 대댓글 달린 피드 주인
                        sendUser, // 대댓글 단 사람
                        feed,
                        reply,
                        reReply,
                        NoticeType.REREPLY,
                        NoticeTarget.POST // 피드 주인을 대상으로 함
                )
        );
    }

    public void saveNoticeForReplyOwner(
            User sendUser, Feed feed, Reply reply, Rereply reReply
    ) {
        noticeRepository.save(
                buildReReplyNotice(
                        reply.getUser().getId(), // 대댓글 달린 댓글 주인
                        sendUser, // 대댓글 단 사람
                        feed,
                        reply,
                        reReply,
                        NoticeType.REREPLY,
                        NoticeTarget.REPLY // 댓글 주인을 대상으로 함
                )
        );
    }

    public NumberOfNoticeResponse getNumberOfNotice(Long noticeOwnerId) {
        return NumberOfNoticeResponse.from(
                noticeRepository.countAllByNoticeOwnerId(noticeOwnerId)
        );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> BaseException.USER_NOT_FOUND);
    }

    private Feed getFeed(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(
                () -> BaseException.FEED_NOT_FOUND);
    }

    private Reply getReply(Long replyId) {
        return replyRepository.findById(replyId).orElseThrow(
                () -> BaseException.REPLY_NOT_FOUND
        );
    }

    private Rereply getReReply(Long reReplyId) {
        return rereplyRepository.findById(reReplyId).orElseThrow(
                () -> BaseException.REREPLY_NOT_FOUND
        );
    }

    private Notice buildReReplyNotice(
            Long ownerUserId, User sendUser, Feed feed, Reply reply,
            Rereply reReply, NoticeType noticeType, NoticeTarget noticeTarget
    ) {
        return Notice.builder()
                .noticeOwnerId(ownerUserId)
                .user(sendUser)
                .feed(feed)
                .reply(reply)
                .rereply(reReply)
                .noticeType(noticeType)
                .noticeTarget(noticeTarget)
                .build();
    }
}
