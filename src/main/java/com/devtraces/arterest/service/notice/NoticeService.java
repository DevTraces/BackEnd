package com.devtraces.arterest.service.notice;

import com.devtraces.arterest.common.exception.BaseException;
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

        // ????????? ?????? ????????? ?????? ????????? ?????? ?????? ?????? ??????
        if (!sendUserId.equals(feed.getUser().getId())) {
            noticeRepository.save(
                    Notice.builder()
                            .noticeOwnerId(feed.getUser().getId()) // ????????? ?????? ????????? ??????
                            .user(getUser(sendUserId)) // ????????? ?????? ?????????
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
                        .noticeOwnerId(ownerUser.getId()) // ????????? ?????? ??????
                        .user(getUser(sendUserId)) // ????????? ??? ??????
                        .noticeType(NoticeType.FOLLOW)
                        .build()
        );
    }

    public void createReplyNotice(
            Long sendUserId, Long feedId, Long replyId
    ) {
        Feed feed = getFeed(feedId);

        // ?????? ????????? ?????? ?????? ????????? ?????? ????????? ?????? ??????
        if (!sendUserId.equals(feed.getUser().getId())) {
            noticeRepository.save(
                    Notice.builder()
                            .noticeOwnerId(feed.getUser().getId()) // ?????? ??? ????????? ??????
                            .user(getUser(sendUserId)) // ?????? ??? ??????
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

        // feed ???????????? ?????? ??????
        saveNoticeForFeedOwner(sendUser, feed, reply, reReply);

        // ?????? ???????????? ?????? ??????
        saveNoticeForReplyOwner(sendUser, feed, reply, reReply);
    }

    public void saveNoticeForFeedOwner(
            User sendUser, Feed feed, Reply reply, Rereply reReply
    ) {
        // ????????? ???????????? ?????? ????????? ?????? ????????? ?????? ??????
        if (!sendUser.getId().equals(feed.getUser().getId())) {
            noticeRepository.save(
                    buildReReplyNotice(
                            feed.getUser().getId(), // ????????? ?????? ?????? ??????
                            sendUser, // ????????? ??? ??????
                            feed,
                            reply,
                            reReply,
                            NoticeTarget.POST // ?????? ????????? ???????????? ???
                    )
            );
        }
    }

    public void saveNoticeForReplyOwner(
            User sendUser, Feed feed, Reply reply, Rereply reReply
    ) {
        // ????????? ???????????? ?????? ????????? ?????? ????????? ?????? ??????
        if (!sendUser.getId().equals(reply.getUser().getId())) {
            noticeRepository.save(
                    buildReReplyNotice(
                            reply.getUser().getId(), // ????????? ?????? ?????? ??????
                            sendUser, // ????????? ??? ??????
                            feed,
                            reply,
                            reReply,
                            NoticeTarget.REPLY // ?????? ????????? ???????????? ???
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
                // ?????? ?????? ????????? ?????? ????????? ????????? ?????????
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

        // ????????? ????????? ?????? ??????
        if (!notice.getNoticeOwnerId().equals(userId)) {
            throw BaseException.FORBIDDEN;
        }

        noticeRepository.delete(notice);
    }

    // ???????????? ????????? ????????? ??? ?????? ????????? ??????
    @Transactional
    public void deleteNoticeWhenFollowingCanceled(
            Long noticeOwnerId, Long userId
    ) {
        List<Notice> notices = noticeRepository
                .findAllByNoticeOwnerIdAndUserId(noticeOwnerId, userId);

        // ????????? ????????? ????????? ?????? ????????? ?????? ??? FOLLOW ????????? ??????
        for (Notice notice : notices) {
            if (notice.getNoticeType().equals(NoticeType.FOLLOW)) {
                noticeRepository.delete(notice);
            }
        }
    }

    // ???????????? ???????????? ?????? ???????????? ????????? ????????? ??? ????????? ??????
    // ???????????? ????????? ??? ????????? id??? ?????? ????????? ??? ??????
    @Transactional
    public void deleteNoticeWhenRereplyDeleted(Long rereplyId) {
        noticeRepository.deleteAllByRereplyId(rereplyId);
    }

    @Transactional
    public void deleteNoticeWhenReplyDeleted(Long replyId) {
        noticeRepository.deleteAllByReplyId(replyId);
    }

    @Transactional
    public void deleteNoticeWhenFeedDeleted(Long feedId) {
        List<Notice> notices = noticeRepository.findAllByFeedId(feedId);
        if (notices.size() > 0) {
            noticeRepository.deleteAll(notices);
        }
    }

    @Transactional
    public void deleteNoticeWhenUserDeleted(Long userId) {
        noticeRepository.deleteAllByUser(getUser(userId));
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
            Rereply reReply, NoticeTarget noticeTarget
    ) {
        return Notice.builder()
                .noticeOwnerId(ownerUserId)
                .user(sendUser)
                .feed(feed)
                .reply(reply)
                .rereply(reReply)
                .noticeType(NoticeType.REREPLY)
                .noticeTarget(noticeTarget)
                .build();
    }
}
