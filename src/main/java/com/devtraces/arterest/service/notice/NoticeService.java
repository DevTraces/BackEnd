package com.devtraces.arterest.service.notice;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.common.type.NoticeTarget;
import com.devtraces.arterest.common.type.NoticeType;
import com.devtraces.arterest.controller.notice.dto.LikeNoticeDto;
import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.controller.notice.dto.FollowNoticeDto;
import com.devtraces.arterest.controller.notice.dto.NumberOfNoticeResponse;
import com.devtraces.arterest.controller.notice.dto.ReplyNoticeDto;
import com.devtraces.arterest.controller.notice.dto.RereplyNoticeDto;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.notice.NoticeRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;
    private final FollowRepository followRepository;

    public void createLikeNotice(
            Long sendUserId, Long feedId
    ) {
        Feed feed = getFeed(feedId);

        // 좋아요 누른 사람이 피드 주인이 아닌 경우 알림 생성
        if (!sendUserId.equals(feed.getUser().getId())) {
            noticeRepository.save(
                    Notice.builder()
                            .noticeOwnerId(feed.getUser().getId()) // 좋아요 누른 피드의 주인
                            .user(getUser(sendUserId)) // 좋아요 누른 사용자
                            .feed(feed)
                            .noticeType(NoticeType.LIKE)
                            .build()
            );
        }
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

        // 피드 주인이 아닌 다른 사람이 댓글 달았을 때만 알림
        if (!sendUserId.equals(feed.getUser().getId())) {
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
        // 대댓글 작성자가 피드 주인이 아닌 경우에 알림 생성
        if (!sendUser.getId().equals(feed.getUser().getId())) {
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
    }

    public void saveNoticeForReplyOwner(
            User sendUser, Feed feed, Reply reply, Rereply reReply
    ) {
        // 대댓글 작성자가 댓글 주인이 아닌 경우에 알림 생성
        if (!sendUser.getId().equals(reply.getUser().getId())) {
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
    }

    public NumberOfNoticeResponse getNumberOfNotice(Long noticeOwnerId) {
        return NumberOfNoticeResponse.from(
                noticeRepository.countAllByNoticeOwnerId(noticeOwnerId)
        );
    }

    public List<NoticeListResponse> getNoticeList(
            Long noticeOwnerId, int page, int pageSize
    ) {

        Page<Notice> noticesOfNoticeOwner =
                noticeRepository.findALlByNoticeOwnerId(
                        noticeOwnerId,
                        PageRequest.of(page, pageSize)
                );

        List<NoticeListResponse> noticeListResponse = new ArrayList<>();
        for (Notice notice : noticesOfNoticeOwner) {
            NoticeType noticeType = notice.getNoticeType();

            if (noticeType.equals(NoticeType.LIKE)) {
                noticeListResponse.add(LikeNoticeDto.convertToLikeNotice(notice));
            }

            if (noticeType.equals(NoticeType.FOLLOW)) {
                // 요청 보낸 사람을 내가 팔로우 중인지 아닌지
                boolean isFollowing =
                        followRepository.isFollowing(
                                noticeOwnerId, notice.getUser().getId()) != 0;

                noticeListResponse.add(
                        FollowNoticeDto.convertToFollowNotice(notice, isFollowing)
                );
            }

            if (noticeType.equals(NoticeType.REPLY)) {
                noticeListResponse.add(ReplyNoticeDto.convertToReplyNotice(notice));
            }

            if (noticeType.equals(NoticeType.REREPLY)) {
                noticeListResponse.add(RereplyNoticeDto.convertToRereplyNotice(notice));
            }
        }

        return noticeListResponse;
    }

    @Transactional
    public void deleteNotice(Long userId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(
                () -> BaseException.NOTICE_NOT_FOUND);

        // 본인의 알림만 삭제 가능
        if (!notice.getNoticeOwnerId().equals(userId)) {
            throw BaseException.FORBIDDEN;
        }

        noticeRepository.delete(notice);
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
