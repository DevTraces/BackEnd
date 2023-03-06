package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.feed.dto.response.FeedCreateResponse;
import com.devtraces.arterest.controller.feed.dto.response.FeedUpdateResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.Hashtag;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.hashtag.HashtagService;
import com.devtraces.arterest.service.s3.S3Service;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final LikeNumberCacheRepository likeNumberCacheRepository;
    private final S3Service s3Service;
    private final HashtagRepository hashtagRepository;
    private final FeedHashtagMapRepository feedHashtagMapRepository;

    private final HashtagService hashtagService;

    @Transactional
    public FeedCreateResponse createFeed(
        Long userId, String content, List<MultipartFile> imageFileList, List<String> hashtagList
    ) {
        User authorUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        // 유저가 올린 이미지가 없을 경우, imageUrlBuilder는 toString하면 "" 이렇게 빈 문자열 된다.
        StringBuilder imageUrlBuilder = new StringBuilder();
        if(imageFileList != null){
            for(MultipartFile imageFile : imageFileList){
                imageUrlBuilder.append(s3Service.uploadImage(imageFile));
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
                .numberOfReplies(0)
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

        // 새로 들어온 hashtag 리스트로 newFeed 엔티티의 필드를 초기화 한다.
        initializeHashtagStringField(hashtagList, newFeed);

        // 새로 만들어진 게시물이므로, 좋아요 개수를 레디스에 0으로 캐시 해둔다.
        // 레디스가 다운되어도 게시물 저장 로직 전체가 취소 및 롤백되지 않고 그대로 완료 된다.
        likeNumberCacheRepository.setInitialLikeNumber(newFeed.getId());
        return FeedCreateResponse.from(newFeed, 0L, hashtagList);
    }

    @Transactional
    public FeedUpdateResponse updateFeed(
        Long userId, String content,
        List<MultipartFile> imageFileList,
        List<String> hashtagList,
        List<String> prevImageUrlList,
        List<String> indexList,
        Long feedId
    ) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(
            () -> BaseException.FEED_NOT_FOUND
        );

        if(!Objects.equals(feed.getUser().getId(), userId)){
            throw BaseException.USER_INFO_NOT_MATCH;
        }

        // 기존 사진들 중 유지해야 하는 사진들을 찾아낸다.
        Set<String> imagesToKeepSet = new HashSet<>();
        if(prevImageUrlList != null){
            for(String prevImageUrlInfo : prevImageUrlList){
                imagesToKeepSet.add( prevImageUrlInfo );
            }
        }
        // 기존에 S3에 저장돼 있던 사진들 중 위에서 정의한 셋에 포함돼 있지 않는 이미지들을 삭제한다.
        if(!feed.getImageUrls().equals("")){
            for(String deleteTargetUrl : feed.getImageUrls().split(",")){
                if(!imagesToKeepSet.contains(deleteTargetUrl)){
                    s3Service.deleteImage(deleteTargetUrl);
                }
            }
        }
        // String 배열을 만든 후, 기존 이미지 url들을 정해진 인덱스에 맞게 넣어 둔다.
        int newImageFileCount = imageFileList == null ? 0 : imageFileList.size();
        int existingImageUrlCount = prevImageUrlList == null ? 0 : prevImageUrlList.size();
        String[] resultImageUrlArr = new String[newImageFileCount + existingImageUrlCount];
        if(existingImageUrlCount != 0){
            for(int i=0; i<prevImageUrlList.size(); i++){
                resultImageUrlArr[ Integer.parseInt(indexList.get(i)) ] = prevImageUrlList.get(i);
            }
        }

        // 새로운 이미지들을 S3에 업로드 하면서 resultImageUrlArr의 null 인 칸들에 순서대로 넣어준다.
        if(newImageFileCount != 0){
            for(MultipartFile newImageFile : imageFileList){
                innerFor:
                for(int i=0; i< resultImageUrlArr.length; i++){
                    if(resultImageUrlArr[i]==null){
                        resultImageUrlArr[i] = s3Service.uploadImage(newImageFile);
                        break innerFor;
                    }
                }
            }
        }

        // 삭제될 FeedHashtagMap 데이터 목록을 가져옴.
        List<FeedHashtagMap> feedHashtagMapList = feedHashtagMapRepository.findByFeed(feed);

        // 기존에 FeedHashtagMap 엔티티들을 전부 삭제한다.
        feedHashtagMapRepository.deleteAllByFeedId(feedId);

        // 사용되지 않는 Hashtag 삭제.
        hashtagService.deleteNotUsingHashtag(feedHashtagMapList);

        // 그 후 입력 받은 값에 따라서 새롭게 저장한다.
        if(hashtagList != null){
            for(String hashtagInputString : hashtagList){
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

        initializeHashtagStringField(hashtagList, feed);

        feed.updateContent(content);
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
        return FeedUpdateResponse.from( feedRepository.save(feed), hashtagList, content );
    }

    private static void initializeHashtagStringField(List<String> hashtagList, Feed feed) {
        if(hashtagList != null){
            StringBuilder builder = new StringBuilder();
            for(String hashtagString : hashtagList){
                builder.append(hashtagString);
                builder.append(",");
            }
            feed.setHashtagStringValues(builder.toString());
        }
    }
}
