package com.devtraces.arterest.service;

import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.like.Likes;
import com.devtraces.arterest.domain.like.LikeRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.dto.feed.FeedResponse;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;
    private final LikeRepository likeRepository;

    /*
    *
    * */
    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedResponseList(Long userId, PageRequest pageRequest){
        System.out.println("겟피드리스트 서비스 메서드 호출");
        // 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
        // 레디스가 셋팅된 후에는 레디스에 캐시된 좋아요 게시물 주키 번호 리스트를 먼저 보고, 없을 때만 DB 보게 만든다.
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        // 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
        // 현재 레디스 셋팅이 완료되지 않았으므로 DB에서 좋아요 개수를 찾아내게 만든다.
        return feedRepository.findAllByAuthorId(userId, pageRequest).stream().map(
            feed -> FeedResponse.from(
                feed, likedFeedSet, likeRepository.countByFeedId(feed.getId())
        )).collect(Collectors.toList());
    }

    // 레디스 추가 후 수정 필요.
    @Transactional(readOnly = true)
    public FeedResponse getOneFeed(Long userId, Long feedId){
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        //커스텀 예외 사용방법 확정 후 수정 필요.
        return FeedResponse.from(
            feedRepository.findById(feedId).orElseThrow(RuntimeException::new),
            likedFeedSet, likeRepository.countByFeedId(feedId)
        );
    }

    /*
     * 엔티티 매핑관계에서 Cascade 옵션을 ALL로 설정해서 피드가 사라지면 연관돼 있는 댓글, 대댓글도 모두 삭제되게
     * 만드는 방법이 있음. 그러나 피드, 댓글, 대댓글은 유저 엔티티와도 연관관계가 존재하기 때문에 Cascade ALL 옵션이
     * 예상치 못한 방향으로 작동할 수 있고, 이 또한 동기식으로 처리될 것이기 때문에 상당한 시간을 필요로 할 것임.
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
    public Feed deleteFeed(Long userId, Long feedId){
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            // BaseException 클래스에 커스텀 예외들을 어떻게 상수화 할 것인지 논의를 끝낸 후 수정.
            () -> new RuntimeException("feed not found")
        );
        if(!Objects.equals(feed.getUser().getId(), userId)){
            // 상수화한 커스텀 예외를 어떻게 사용할 것인지에 대한 합의가 이루어진 후 수정할 예정.
            throw new RuntimeException("다른 사람의 게시물은 삭제할 수 없습니다.");
        }

        // 좋아요 테이블에서 정보 삭제
        likeRepository.deleteAllByFeedId(feedId);

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
