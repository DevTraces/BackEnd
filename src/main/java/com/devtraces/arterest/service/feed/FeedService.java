package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.component.S3Util;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.create.FeedCreateResponse;
import com.devtraces.arterest.controller.feed.dto.FeedResponse;
import com.devtraces.arterest.controller.feed.dto.update.ExistingImageUrlDto;
import com.devtraces.arterest.controller.feed.dto.update.FeedUpdateRequest;
import com.devtraces.arterest.controller.feed.dto.update.FeedUpdateResponse;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.domain.hashtag.Hashtag;
import com.devtraces.arterest.domain.hashtag.HashtagRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final S3Util s3Util;
    private final HashtagRepository hashtagRepository;
    private final FeedHashtagMapRepository feedHashtagMapRepository;

    @Transactional
    public FeedCreateResponse createFeed(
        Long userId, String content, List<MultipartFile> imageFileList, List<String> hashtagList
    ) {
        // 게시물 텍스트 없이 사진 또는 해시태그만 게시물로서 올리고 싶은 유저가 분명 있을 것이므로,
        // content가 빈 스트링인 것에 대해서도 받아들인다.
        if(content.length() > CommonUtils.CONTENT_LENGTH_LIMIT){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        if(hashtagList != null && hashtagList.size() > CommonUtils.HASHTAG_COUNT_LIMIT){
            throw BaseException.HASHTAG_LIMIT_EXCEED;
        }
        if(imageFileList != null && imageFileList.size() > CommonUtils.IMAGE_FILE_COUNT_LIMIT){
            throw BaseException.IMAGE_FILE_COUNT_LIMIT_EXCEED;
        }
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );

        // 유저가 올린 이미지가 없을 경우, imageUrlBuilder는 toString하면 "" 이렇게 빈 문자열 된다.
        StringBuilder imageUrlBuilder = new StringBuilder();
        if(imageFileList != null){
            for(MultipartFile imageFile : imageFileList){
                imageUrlBuilder.append(s3Util.uploadImage(imageFile));
                imageUrlBuilder.append(',');
            }
        }

        // 유저가 올린 이미지가 없을 경우, 최종적으로 프런트엔드가 받는 JSON에서는
        // "imageUrls" : null이 리턴된다.
        Feed newFeed = feedRepository.save(
            Feed.builder()
                .content(content)
                .imageUrls(imageUrlBuilder.toString())
                .user(authorUser)
                .build()
        );

        // 입력 받은 해시태그 값들을 순회하면서 새로 저장해야 하는 것은 저장하고, 이미 찾을 수 있는 것은 찾아내서
        // FeedHashtagMap에 저장한다.
        // FeedHashtagMap 엔티티를 빌드하기 위해서는 Feed 엔티티와 Hashtag 엔티티 모두가 필요하다.
        if(hashtagList != null){
            for(String hashtagInputString : hashtagList){
                Hashtag hashtagEntity = hashtagRepository.findByHashtagString(hashtagInputString).orElse(
                    hashtagRepository.save(
                        Hashtag.builder()
                            .hashtagString(hashtagInputString)
                            .build()
                    )
                );
                feedHashtagMapRepository.save(
                    FeedHashtagMap.builder()
                        .feed(newFeed)
                        .hashtag(hashtagEntity)
                        .build()
                );
            }
        }

        // 새로 만들어진 게시물이므로, 좋아요 개수를 레디스에 0으로 캐시 해둔다.
        // 레디스가 다운되어도 게시물 저장 로직 전체가 취소 및 롤백되지 않고 그대로 완료 된다.
        likeNumberCacheRepository.setInitialLikeNumber(newFeed.getId());

        return FeedCreateResponse.from(newFeed, 0L, hashtagList);
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedResponseList(Long userId, int page, int pageSize){
        // 요청한 사용자가 좋아요를 누른 피드들의 주키 아이디 번호들을 먼저 불러온다.
        Set<Long> likedFeedSet = likeRepository.findAllByUserId(userId)
            .stream().map(Likes::getFeedId).collect(Collectors.toSet());

        // 요청한 사용자가 북마크 했던 피드들의 주키 아이디 번호들도 불러온다.
        Set<Long> bookmarkedFeedSet = bookmarkRepository.findAllByUserId(userId)
            .stream().map(bookmark -> bookmark.getFeed().getId()).collect(Collectors.toSet());

        // 피드 별 좋아요 개수는 레디스를 먼저 보게 만들고, 그게 불가능 할때만 Like 테이블에서 찾도록 한다.
        return feedRepository.findAllByUserId(userId, PageRequest.of(page, pageSize)).stream().map(
            feed -> {
                Long likeNumber = likeNumberCacheRepository.getFeedLikeNumber(feed.getId());
                if(likeNumber == null) {
                    likeNumber = likeRepository.countByFeedId(feed.getId());
                    // 현재 캐시서버에 좋아요 개수가 기록돼 있지 않으므로 다음 read 요쳥을 위해서 캐시해 둠.
                    likeNumberCacheRepository.setInitialLikeNumber(likeNumber);
                }
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
            likeNumberCacheRepository.setInitialLikeNumber(likeNumber);
        }

        return FeedResponse.from(
            feed, likedFeedSet, likeNumber, bookmarkedFeedSet
        );
    }

    @Transactional
    public FeedUpdateResponse updateFeed(
        Long userId,
        List<MultipartFile> imageFileList,
        FeedUpdateRequest feedUpdateRequest,
        Long feedId
    ) {
        if(
            feedUpdateRequest.getContent() != null &&
            feedUpdateRequest.getContent().length() > CommonUtils.CONTENT_LENGTH_LIMIT
        ){
            throw BaseException.CONTENT_LIMIT_EXCEED;
        }
        if(
            feedUpdateRequest.getHashtags() != null &&
            feedUpdateRequest.getHashtags().size() > CommonUtils.HASHTAG_COUNT_LIMIT
        ){
            throw BaseException.HASHTAG_LIMIT_EXCEED;
        }
        if(imageFileList != null && imageFileList.size() > CommonUtils.IMAGE_FILE_COUNT_LIMIT){
            throw BaseException.IMAGE_FILE_COUNT_LIMIT_EXCEED;
        }

        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );

        if(!Objects.equals(feed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // 기존 사진들 중 유지해야 하는 사진들을 찾아낸다.
        Set<String> imagesToKeepSet = new HashSet<>();
        if(feedUpdateRequest.getExistingImageUrls() != null){
            for(ExistingImageUrlDto dto : feedUpdateRequest.getExistingImageUrls()){
                imagesToKeepSet.add(dto.getImageUrl());
            }
        }

        // 기존에 S3에 저장돼 있던 사진들 중 위에서 정의한 셋에 포함돼 있지 않는 이미지들을 삭제한다.
        if(!feed.getImageUrls().equals("")){
            for(String deleteTargetUrl : feed.getImageUrls().split(",")){
                if(!imagesToKeepSet.contains(deleteTargetUrl)){
                    s3Util.deleteImage(deleteTargetUrl);
                }
            }
        }

        // String 배열을 만든 후, 기존 이미지 url들을 정해진 인덱스에 맞게 넣어 둔다.
        int newImageFileCount = imageFileList == null ? 0 : imageFileList.size();
        int existingImageUrlCount = feedUpdateRequest.getExistingImageUrls() == null ? 0 :
            feedUpdateRequest.getExistingImageUrls().size();

        String[] resultImageUrlArr = new String[newImageFileCount + existingImageUrlCount];

        if(existingImageUrlCount != 0){
            for(ExistingImageUrlDto dto : feedUpdateRequest.getExistingImageUrls()){
                resultImageUrlArr[dto.getIndex()] = dto.getImageUrl();
            }
        }

        // 새로운 이미지들을 S3에 업로드 하면서 resultImageUrlArr의 null 인 칸들에 순서대로 넣어준다.
        if(newImageFileCount != 0){
            for(MultipartFile newImageFile : imageFileList){
                innerFor:
                for(int i=0; i< resultImageUrlArr.length; i++){
                    if(resultImageUrlArr[i]==null){
                        resultImageUrlArr[i] = s3Util.uploadImage(newImageFile);
                        break innerFor;
                    }
                }
            }
        }

        // 기존에 FeedHashtagMap 엔티티들을 전부 삭제한다.
        feedHashtagMapRepository.deleteAllByFeedId(feedId);

        // 그 후 입력 받은 값에 따라서 새롭게 저장한다.
        if(feedUpdateRequest.getHashtags() != null){
            for(String hashtagInputString : feedUpdateRequest.getHashtags()){
                Optional<Hashtag> optionalHashtag = hashtagRepository
                    .findByHashtagString(hashtagInputString);
                if(optionalHashtag.isPresent()){
                    // 해시태그 엔티티를 찾을 수 있는 경우.
                    feedHashtagMapRepository.save(
                        FeedHashtagMap.builder()
                            .feed(feed)
                            .hashtag(optionalHashtag.get())
                            .build()
                    );
                } else { // 해시태그 엔티티를 찾을 수 없는 경우.
                    Hashtag newHashtag = hashtagRepository.save(
                        Hashtag.builder()
                            .hashtagString(hashtagInputString)
                            .build()
                    );
                    feedHashtagMapRepository.save(
                        FeedHashtagMap.builder()
                            .feed(feed)
                            .hashtag(newHashtag)
                            .build()
                    );
                }
            }
        }

        feed.updateContent(feedUpdateRequest.getContent());

        // Feed 엔티티의 imageUrls 필드의 내용물을 수정할 때 사용할 문자열을 만들어 준다.
        StringBuilder imageUrlBuilder = new StringBuilder();
        if(resultImageUrlArr.length != 0){
            for(String resultImageUrl : resultImageUrlArr){
                imageUrlBuilder.append(resultImageUrl);
                imageUrlBuilder.append(',');
            }
        }

        feed.updateImageUrls(
            imageUrlBuilder.toString().equals("") ? null : imageUrlBuilder.toString()
        );

        return FeedUpdateResponse.from( feedRepository.save(feed), feedUpdateRequest.getHashtags() );
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

        // S3에 올려놨던 사진들을 전부 삭제한다.
        if(!feed.getImageUrls().equals("")){
            for(String deleteTargetUrl : feed.getImageUrls().split(",")){
                s3Util.deleteImage(deleteTargetUrl);
            }
        }

        // FeedHashtagMap 테이블에서 관련 정보 모두 삭제.
        feedHashtagMapRepository.deleteAllByFeedId(feedId);

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
