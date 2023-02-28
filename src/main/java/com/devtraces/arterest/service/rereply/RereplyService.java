package com.devtraces.arterest.service.rereply;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.controller.rereply.dto.request.RereplyRequest;
import com.devtraces.arterest.controller.rereply.dto.response.RereplyResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.devtraces.arterest.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RereplyService {

    private final RereplyRepository rereplyRepository;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final NoticeService noticeService;

    @Transactional
    public RereplyResponse createRereply(
        Long userId, Long feedId, Long replyId, RereplyRequest rereplyRequest) {
        validateRereplyRequest(rereplyRequest);
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Reply reply = replyRepository.findById(replyId).orElseThrow(
            () -> BaseException.REPLY_NOT_FOUND
        );
        Rereply rereply = rereplyRepository.save(
            Rereply.builder()
                .content(rereplyRequest.getContent())
                .user(authorUser)
                .reply(reply)
                .build()
        );

        noticeService.createReReplyNotice(authorUser.getId(), feedId, reply.getId(), rereply.getId());

        return RereplyResponse.from(rereply, feedId);
    }

    @Transactional(readOnly = true)
    public List<RereplyResponse> getRereplyList(
        Long feedId, Long replyId, Integer page, Integer pageSize
    ) {
        return rereplyRepository.findAllByReplyId(replyId, PageRequest.of(page, pageSize))
            .getContent().stream().map(
                rereply -> RereplyResponse.from(rereply, feedId)
            ).collect(Collectors.toList());
    }

    @Transactional
    public RereplyResponse updateRereply(
        Long userId, Long feedId, Long rereplyId, RereplyRequest rereplyRequest
    ) {
        validateRereplyRequest(rereplyRequest);
        Rereply rereply = rereplyRepository.findById(rereplyId).orElseThrow(
            () -> BaseException.REREPLY_NOT_FOUND
        );
        if(!Objects.equals(rereply.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }
        rereply.updateContent(rereplyRequest.getContent());
        return RereplyResponse.from(rereplyRepository.save(rereply), feedId);
    }

    @Transactional
    public void deleteRereply(Long userId, Long rereplyId) {
        Rereply rereply = rereplyRepository.findById(rereplyId).orElseThrow(
            () -> BaseException.REREPLY_NOT_FOUND
        );
        if(!Objects.equals(rereply.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }
        rereplyRepository.deleteById(rereplyId);
    }

    @Transactional
    public void deleteAllFeedRelatedRereply(Feed feed){
        for(Reply reply : feed.getReplyList()){
            if(reply.getRereplyList().size() > 0){
                rereplyRepository.deleteAllByIdIn(
                    reply.getRereplyList().stream().map(Rereply::getId)
                        .collect(Collectors.toList())
                );
            }
        }
    }

    private void validateRereplyRequest(RereplyRequest rereplyRequest) {
        if(rereplyRequest.getContent() == null || rereplyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
        if(rereplyRequest.getContent().length() > CommonConstant.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
    }
}
