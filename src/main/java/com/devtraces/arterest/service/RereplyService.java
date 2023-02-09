package com.devtraces.arterest.service;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.dto.rereply.RereplyRequest;
import com.devtraces.arterest.dto.rereply.RereplyResponse;
import java.util.List;
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
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Reply reply = replyRepository.findById(replyId).orElseThrow(
            () -> BaseException.REPLY_NOT_FOUND
        );
        Rereply rereply = rereplyRepository.save(
            Rereply.builder()
                .replyId(replyId)
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
}
