package com.devtraces.arterest.service;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.dto.feed.FeedResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;

    public List<FeedResponse> getFeedResponseList(){
        return null;
    }

    /*
     * 엔티티 매핑관계에서 Cascade 옵션을 ALL로 설정해서 피드가 사라지면 연관돼 있는 댓글, 대댓글도 모두 삭제되게
     * 만드는 방법이 있음. 그러나 피드, 댓글, 대댓글은 유저 엔티티와도 연관관계가 존재하기 때문에 Cascade ALL 옵션이
     * 예상치 못한 방향으로 작동할 수 있고, 이 또한 동기처리될 것이기 때문에 상당한 시간을 필요로 할 것임.
     * 그리고 이는 유저의 탈퇴와도 관련이 깊어서 유저 관련 API가 완성된 후에 다시 생각해볼 필요가 있음.
     * 우선 안전한 방법으로 삭제하는 것으로 구현하고 추후 Cascade 옵션에 대한 테스트 후 결정을 진행함.
     *
     * 만약 특정 피드가 댓글과 대댓글이 수만개에 달하는 대형 객체라면 스프링에서 제공하는 @Async를 사용하여
     * 멀티스레딩 비동기 처리로 응답지연시간을 최소화 시길 수 있음.
     *
     * 가장 좋은 방법은 유저 접속시간이 적은 시간에 삭제 작업만을 전담하는 배치서버를 따로 만드는 것이지만
     * 그러기에는 시간이 없음.
     *
     * 그리고 만약 이러한 대형 피드를 수 백개 씩 보유하고 있는 셀럽 유저가 탈퇴를 한 상황에서 이 유저의 모든 피드를 지우는 작업을
     * 비동기가 아닌 동기 식으로 처리한다면 서버에 큰 부담을 줄 수 있음.
     * */
    @Transactional
    public Feed deleteFeed(Long feedId){
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            // BaseException 클래스에 커스텀 예외들을 어떻게 상수화 할 것인지 논의를 끝낸 후 수정.
            () -> new RuntimeException("feed not found")
        );

        // 대댓글 삭제
        for(Reply reply : feed.getReplyList()){
            if(reply.getRereplyList().size() > 0){
                rereplyRepository.deleteAllByIdIn(
                    reply.getRereplyList().stream().map(Rereply::getId)
                        .collect(Collectors.toList())
                );
            }
        }

        // 댓글 삭제
        replyRepository.deleteAllByIdIn(
            feed.getReplyList().stream().map(Reply::getId).collect(Collectors.toList())
        );

        // 피드 삭제.
        feedRepository.deleteById(feedId);
        return feed;
    }

}
