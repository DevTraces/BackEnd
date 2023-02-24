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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;

    @Transactional
    public ReplyResponse createReply(Long userId, Long feedId, ReplyRequest replyRequest) {
        if(replyRequest.getContent() == null || replyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
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
                .build()
        );
        return ReplyResponse.from(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getReplyList(Long feedId, Integer page, Integer pageSize) {
        return replyRepository
            .findAllByFeedIdOrderByCreatedAtDesc(feedId, PageRequest.of(page, pageSize))
            .getContent().stream().map(ReplyResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ReplyResponse updateReply(Long userId, Long replyId, ReplyRequest replyRequest) {
        if(replyRequest.getContent() == null || replyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
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

    // TODO 이 작업도 @Async 로 비동기 멀티 스레딩 처리를 할 경우 응답 지연 시간을 최소화 할 수 있다.
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

        // 댓글을 삭제한다.
        replyRepository.deleteById(replyId);
    }
}
