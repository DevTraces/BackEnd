package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.component.S3Uploader;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.FeedResponse;
import com.devtraces.arterest.controller.feed.dto.FeedUpdateResponse;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.like.LikeRepository;
import com.devtraces.arterest.domain.like.Likes;
import com.devtraces.arterest.domain.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ReplyRepository replyRepository;
    private final RereplyRepository rereplyRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeNumberCacheRepository likeNumberCacheRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public FeedResponse createFeed(
        Long userId, String content, List<MultipartFile> imageFileList, List<String> hashtagList
    ) {
        if(content.length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        if(hashtagList.size() > CommonUtils.HASHTAG_COUNT_LIMIT){
            throw BaseException.HASHTAG_LIMIT_EXCEED;
        }
        if(imageFileList.size() > CommonUtils.IMAGE_FILE_COUNT_LIMIT){
            throw BaseException.IMAGE_FILE_COUNT_LIMIT_EXCEED;
        }
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );

        StringBuilder imageUrlBuilder = new StringBuilder();
        for(MultipartFile imageFile : imageFileList){
            imageUrlBuilder.append(s3Uploader.uploadImage(imageFile));
            imageUrlBuilder.append(',');
        }

        StringBuilder hashtagBuilder = new StringBuilder();
        for(String hashtag : hashtagList){
            hashtagBuilder.append(hashtag);
            hashtagBuilder.append(',');
        }

        Feed newFeed = feedRepository.save(
            Feed.builder()
                .content(content)
                .imageUrls(imageUrlBuilder.toString())
                .hashtags(hashtagBuilder.toString())
                .user(authorUser)
                .build()
        );

        likeNumberCacheRepository.setInitialLikeNumber(newFeed.getId());

        return FeedResponse.from(newFeed, null, 0L, null);
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedResponseList(Long userId, PageRequest pageRequest){
        // 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        // 요청한 사용자가 북마크 했던 피드들의 주키 아이디 번호들도 불러온다.
        Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
            .stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

        // 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
        return feedRepository.findAllByUserId(userId, pageRequest).stream().map(
            feed -> {
                Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feed.getId());
                if(likeNumber == null) likeNumber = likeRepository.countByFeedId(feed.getId());
                return FeedResponse.from(feed, likedFeedSet, likeNumber, bookmarkedFeedSet);
            }
        ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedResponse getOneFeed(Long userId, Long feedId){
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
            .stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> BaseException.FEED_NOT_FOUND);

        Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feedId);
        if(likeNumber == null) {
            likeNumber = likeRepository.countByFeedId(feedId);
        }

        return FeedResponse.from(
            feed, likedFeedSet, likeNumber, bookmarkedFeedSet
        );
    }

    @Transactional
    public FeedUpdateResponse updateFeed(
        Long userId, String content,
        List<MultipartFile> imageFileList, List<String> hashtagList,
        Long feedId
    ) {
        if(content.length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        if(hashtagList.size() > CommonUtils.HASHTAG_COUNT_LIMIT){
            throw BaseException.HASHTAG_LIMIT_EXCEED;
        }
        if(imageFileList.size() > CommonUtils.IMAGE_FILE_COUNT_LIMIT){
            throw BaseException.IMAGE_FILE_COUNT_LIMIT_EXCEED;
        }

        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );

        if(!Objects.equals(feed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // TODO 기존에 S3에 저장돼 있던 사진들을 삭제하는 로직이 필요하다.

        // 기존 이미지들의 삭제가 진행되었다고 가정하고 새 이미지를 올린다.
        StringBuilder imageUrlBuilder = new StringBuilder();
        for(MultipartFile imageFile : imageFileList){
            imageUrlBuilder.append(s3Uploader.uploadImage(imageFile));
            imageUrlBuilder.append(',');
        }

        StringBuilder hashtagBuilder = new StringBuilder();
        for(String hashtag : hashtagList){
            hashtagBuilder.append(hashtag);
            hashtagBuilder.append(',');
        }

        feed.updateContent(content);
        feed.updateImageUrls(imageUrlBuilder.toString());
        feed.updateHashtags(hashtagBuilder.toString());

        return FeedUpdateResponse.from( feedRepository.save(feed) );
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
}
