package com.devtraces.arterest.service.rereply;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.controller.rereply.dto.RereplyRequest;
import com.devtraces.arterest.controller.rereply.dto.RereplyResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    @Transactional
    public RereplyResponse createRereply(
        Long userId, Long feedId, Long replyId, RereplyRequest rereplyRequest) {
        if(rereplyRequest.getContent() == null || rereplyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
        if(rereplyRequest.getContent().length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
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
        if(rereplyRequest.getContent() == null || rereplyRequest.getContent().equals("")){
            throw BaseException.NULL_AND_EMPTY_STRING_NOT_ALLOWED;
        }
        if(rereplyRequest.getContent().length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
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
}
