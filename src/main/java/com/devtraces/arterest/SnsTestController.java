package com.devtraces.arterest;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SnsTestController {

    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;

    @GetMapping("/run-test-controller")
    @Transactional
    public void run(){
        // 1번 피드 생성
        Feed oneFeed = Feed.builder()
            .build();

        feedRepository.save(oneFeed);


        // 피드에 1,2번 댓글 작성
        Reply oneReply = Reply.builder()
            .feed(oneFeed)
            .build();
        replyRepository.save(oneReply);

        Reply twoReply = Reply.builder()
            .feed(oneFeed)
            .build();
        replyRepository.save(twoReply);


        // 1번 댓글에 2개 대댓글 생성
        Rereply oneRereply = Rereply.builder()
            .reply(oneReply)
            .build();
        rereplyRepository.save(oneRereply);

        Rereply twoRereply = Rereply.builder()
            .reply(oneReply)
            .build();
        rereplyRepository.save(twoRereply);
    }

}
