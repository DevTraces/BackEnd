package com.devtraces.arterest.service;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.devtraces.arterest.dto.reply.ReplyRequest;
import com.devtraces.arterest.dto.reply.ReplyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;

    public ReplyResponse createReply(Long userId, Long feedId, ReplyRequest replyRequest) {
        if(replyRequest.getContent().length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );
        return ReplyResponse.from(
            replyRepository.save(
                Reply.builder()
                    .content(replyRequest.getContent())
                    .user(authorUser)
                    .feed(feed)
                    .build()
            )
        );
    }

    
}
