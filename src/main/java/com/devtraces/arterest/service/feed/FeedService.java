package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.FeedResponse;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.like.LikeRepository;
import com.devtraces.arterest.domain.like.Likes;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
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
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedResponseList(Long userId, PageRequest pageRequest){
        // 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
        // 레디스가 셋팅된 후에는 레디스에 캐시된 좋아요 게시물 주키 번호 리스트를 먼저 보고, 없을 때만 DB 보게 만든다.
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
            .stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

        // 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
        // 현재 레디스 셋팅이 완료되지 않았으므로 DB에서 좋아요 개수를 찾아내게 만든다.
        return feedRepository.findAllByUserId(userId, pageRequest).stream().map(
            feed -> FeedResponse.from(
                feed, likedFeedSet, likeRepository.countByFeedId(feed.getId()), bookmarkedFeedSet
            )).collect(Collectors.toList());
    }

    // 레디스 추가 후 좋아요 개수를 레디스에서 먼저 얻어오도록 수정하는 것 필요.
    @Transactional(readOnly = true)
    public FeedResponse getOneFeed(Long userId, Long feedId){
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
            .stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> BaseException.FEED_NOT_FOUND);

        //커스텀 예외 사용방법 확정 후 수정 필요.
        return FeedResponse.from(
            feed, likedFeedSet, likeRepository.countByFeedId(feedId), bookmarkedFeedSet
        );
    }

    // TODO 스프링 @Async를 사용해서 비동기 멀티 스레딩으로 처리하면 응답지연시간 최소화 가능.
    @Transactional
    public void deleteFeed(Long userId, Long feedId){
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );
        if(!Objects.equals(feed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // 좋아요 테이블에서 정보 모두 삭제
        likeRepository.deleteAllByFeedId(feedId);

        // 북마크 테이블에서 정보 모두 삭제
        bookmarkRepository.deleteAllByFeedId(feedId);

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
    }

}
