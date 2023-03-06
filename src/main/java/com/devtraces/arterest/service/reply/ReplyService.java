package com.devtraces.arterest.service.reply;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.controller.reply.dto.request.ReplyRequest;
import com.devtraces.arterest.controller.reply.dto.response.ReplyResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.devtraces.arterest.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;
    private final NoticeService noticeService;

    @Async
    @Transactional
    public ReplyResponse createReply(Long userId, Long feedId, ReplyRequest replyRequest) {
        validateReplyRequest(replyRequest);
        if(replyRequest.getContent().length() > CommonConstant.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );
        Reply reply = replyRepository.save(
            Reply.builder()
                .content(replyRequest.getContent())
                .user(authorUser)
                .feed(feed)
                .feedPrimaryKeyId(feedId)
                .numberOfRereplies(0)
                .build()
        );

        // Feed 엔티티의 댓글 개수 필드 += 1.
        feed.plusOneReply();

        noticeService.createReplyNotice(authorUser.getId(), feed.getId(), reply.getId());

        return ReplyResponse.from(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getReplyList(Long feedId, Integer page, Integer pageSize) {
        return replyRepository
            .findAllReplyJoinUserLatestFirst(feedId, PageRequest.of(page, pageSize))
            .getContent().stream().map(
                replyResponseConverter -> ReplyResponse.fromConverter(
                    replyResponseConverter, feedId
                )
            ).collect(Collectors.toList());
    }

    @Transactional
    public ReplyResponse updateReply(Long userId, Long replyId, ReplyRequest replyRequest) {
        validateReplyRequest(replyRequest);
        if(replyRequest.getContent().length() > CommonConstant.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        Reply reply = replyRepository.findById(replyId).orElseThrow(
            () -> BaseException.REPLY_NOT_FOUND
        );
        if(!Objects.equals(reply.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }
        reply.updateContent(replyRequest.getContent());
        replyRepository.save(reply);
        return ReplyResponse.from(reply);
    }

    @Async
    @Transactional
    public void deleteReply(Long userId, Long replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(
            () -> BaseException.REPLY_NOT_FOUND
        );
        if(!Objects.equals(reply.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }
        // 댓글에 달려 있는 대댓글을 삭제한다.
        if(reply.getRereplyList() != null && reply.getRereplyList().size() > 0){
            rereplyRepository.deleteAllByIdIn(
                reply.getRereplyList().stream().map(Rereply::getId).collect(Collectors.toList())
            );
        }

        // 게시물의 댓글 개수을 1개 차감한다.
        reply.getFeed().minusOneReply();

        // 댓글을 삭제한다.
        replyRepository.deleteById(replyId);
    }

    @Async
    @Transactional
    public void deleteAllFeedRelatedReply(Feed feed){
        replyRepository.deleteAllByIdIn(
            feed.getReplyList().stream().map(Reply::getId).collect(Collectors.toList())
        );
    }

    private static void validateReplyRequest(ReplyRequest replyRequest) {
        if(replyRequest.getContent() == null || replyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
    }
}
