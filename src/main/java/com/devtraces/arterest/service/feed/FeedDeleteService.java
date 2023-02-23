package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedDeleteService {
	private final FeedRepository feedRepository;
	private final ReplyRepository replyRepository;
	private final RereplyRepository rereplyRepository;
	private final LikeRepository likeRepository;
	private final BookmarkRepository bookmarkRepository;
	private final LikeNumberCacheRepository likeNumberCacheRepository;
	private final S3Service s3Service;
	private final FeedHashtagMapRepository feedHashtagMapRepository;
	private final HashtagRepository hashtagRepository;

	// TODO 스프링 @Async를 사용해서 비동기 멀티 스레딩으로 처리하면 응답지연시간 최소화 가능.
	@Transactional
	public void deleteFeed(Long userId, Long feedId){
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> BaseException.FEED_NOT_FOUND
		);
		if(!Objects.equals(feed.getUser().getId(), userId)){
			throw BaseException.USER_INFO_NOT_MATCH;
		}

		// S3에 올려놨던 사진들을 전부 삭제한다.
		if(!feed.getImageUrls().equals("")){
			for(String deleteTargetUrl : feed.getImageUrls().split(",")){
				s3Service.deleteImage(deleteTargetUrl);
			}
		}
		
		// 삭제될 FeedHashtagMap 데이터 목록을 가져옴.
		List<FeedHashtagMap> feedHashtagMapList = feedHashtagMapRepository.findByFeed(feed);
		
		// FeedHashtagMap 테이블에서 관련 정보 모두 삭제.
		feedHashtagMapRepository.deleteAllByFeedId(feedId);

		// 사용되지 않는 Hashtag 삭제.
		deleteNotUsingHashtag(feedHashtagMapList);

		// 레디스에서 좋아요 개수 기록한 키-밸류 쌍 삭제.
		likeNumberCacheRepository.deleteLikeNumberInfo(feedId);

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

	void deleteNotUsingHashtag(List<FeedHashtagMap> feedHashtagMapList){
		// 삭제된 FeedHashtagMap의 feedId에 매핑된 hashtagId가 더이상 FeedHashtagMap에 존재하지 않을 경우,
		// 해당 hastagId를 Hashtag 테이블에서 삭제함.
		for (FeedHashtagMap feedHashtagMap : feedHashtagMapList) {
			if(!feedHashtagMapRepository.existsByHashtag(feedHashtagMap.getHashtag())){
				hashtagRepository.deleteById(feedHashtagMap.getHashtag().getId());
			}
		}
	}
}
