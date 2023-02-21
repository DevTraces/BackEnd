package com.devtraces.arterest.service.feed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.service.s3.S3Service;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.feed.dto.response.FeedResponse;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.model.hashtag.Hashtag;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.like.LikeRepository;
import com.devtraces.arterest.model.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private LikeNumberCacheRepository likeNumberCacheRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private HashtagRepository hashtagRepository;
    @Mock
    private FeedHashtagMapRepository feedHashtagMapRepository;
    @InjectMocks
    private FeedService feedService;

    @Test
    @DisplayName("기존 해시태그 찾아내면서 게시물 1개 생성 성공.")
    void successCreateFeedFoundAlreadySavedHashTag(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        User user = User.builder()
            .id(1L)
            .build();

        Hashtag hashtagEntity = Hashtag.builder()
            .id(1L)
            .hashtagString("#potato")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .content(content)
            .imageUrls("urlString,")
            .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(s3Service.uploadImage(multipartFile)).willReturn("urlString");
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.of(hashtagEntity));
        given(feedRepository.save(any())).willReturn(feed);

        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(1L);

        // when
        feedService.createFeed(1L, content, imageFileList, hashtagList);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(s3Service, times(1)).uploadImage(any());
        verify(hashtagRepository, times(1)).findByHashtagString(anyString());
    }

    @Test
    @DisplayName("해시태그 새로 저장하면서 게시물 1개 생성 성공.")
    void successCreateFeedSaveNewHashTag(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        User user = User.builder()
            .id(1L)
            .build();

        Hashtag hashtagEntity = Hashtag.builder()
            .id(1L)
            .hashtagString("#potato")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .content(content)
            .imageUrls("urlString,")
            .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(s3Service.uploadImage(multipartFile)).willReturn("urlString");
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.empty());
        given(hashtagRepository.save(any())).willReturn(hashtagEntity);
        given(feedRepository.save(any())).willReturn(feed);

        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(1L);

        // when
        feedService.createFeed(1L, content, imageFileList, hashtagList);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(s3Service, times(1)).uploadImage(any());
        verify(hashtagRepository, times(1)).findByHashtagString(anyString());
    }

    @Test
    @DisplayName("게시물 생성 실패 - 내용물 텍스트 길이 제한 초과")
    void failedCreateFeedContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        String content = sb.toString();

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.createFeed(1L, content, imageFileList, hashtagList)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 생성 실패 - 해시태그 개수 초과")
    void failedCreateFeedHashtagCountLimitExceed(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        for(int i=1; i<=11; i++){
            hashtagList.add("#potato");
        }

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.createFeed(1L, content, imageFileList, hashtagList)
        );

        // then
        assertEquals(ErrorCode.HASHTAG_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 생성 실패 - 이미지파일 개수 초과")
    void failedCreateFeedImagefileCountLimitExceed(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
            hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        for(int i=1; i<= 16; i++){
            imageFileList.add(multipartFile);
        }

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.createFeed(1L, content, imageFileList, hashtagList)
        );

        // then
        assertEquals(ErrorCode.IMAGE_FILE_COUNT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 생성 실패 - 사용자를 찾을 수 없음.")
    void failedCreateFeedUserNotFound(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.createFeed(1L, content, imageFileList, hashtagList)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("피드 리스트 읽기 성공 - 레디스에서 좋아요 개수 획득 성공한 경우.")
    void successGetFeedListRedisServerAvailable(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(replyList)
            .imageUrls("url2,url3")
            .user(user)
            .build();

        List<Feed> feedList = new ArrayList<>();
        feedList.add(feed);

        Slice<Feed> slice = new PageImpl<>(feedList);

        given(feedRepository.findAllByUserId(1L, PageRequest.of(0, 10))).willReturn(slice);
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);
        //given(likeRepository.countByFeedId(1L)).willReturn(0L);

        //when
        List<FeedResponse> feedResponseList = feedService.getFeedResponseList(1L, 0, 10);

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(feedRepository, times(1)).findAllByUserId(1L, PageRequest.of(0, 10));
        assertEquals(feedResponseList.size(), 1);
    }

    @Test
    @DisplayName("피드 리스트 읽기 성공 - 레디스에서 좋아요 개수 획득에 실패한 경우.")
    void successGetFeedListRedisServerNotAvailable(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(replyList)
            .imageUrls("url2,url3")
            .user(user)
            .build();

        List<Feed> feedList = new ArrayList<>();
        feedList.add(feed);

        Slice<Feed> slice = new PageImpl<>(feedList);

        given(feedRepository.findAllByUserId(1L, PageRequest.of(0, 10))).willReturn(slice);
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(0L);

        //when
        List<FeedResponse> feedResponseList = feedService.getFeedResponseList(1L, 0, 10);

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).setInitialLikeNumber(0L);
        verify(feedRepository, times(1)).findAllByUserId(1L, PageRequest.of(0, 10));
        assertEquals(feedResponseList.size(), 1);
    }

    @Test
    @DisplayName("피드 1개 읽기 성공 - 레디스에서 좋아요 개수 획득에 성공한 경우.")
    void successGetOneFeedRedisServerAvailable(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(replyList)
            .imageUrls("url2,url3")
            .user(user)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(0L);
        //given(likeRepository.countByFeedId(1L)).willReturn(0L);

        //when
        FeedResponse feedResponse = feedService.getOneFeed(2L, 1L);

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        //verify(likeRepository, times(1)).countByFeedId(1L);
        verify(feedRepository, times(1)).findById(1L);
        assertEquals(feedResponse.getFeedId(), 1L);
    }

    @Test
    @DisplayName("피드 1개 읽기 성공 - 레디스에서 좋아요 개수 획득에 실패한 경우")
    void successGetOneFeedRedisServerNotAvailable(){
        //given
        Reply reply = Reply.builder()
            .id(1L)
            .content("this is reply")
            .build();
        List<Reply> replyList = new ArrayList<>();
        replyList.add(reply);

        User user = User.builder()
            .id(1L)
            .description("introduction")
            .profileImageUrl("url1")
            .nickname("dongvin99")
            .username("박동빈")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(replyList)
            .imageUrls("url2,url3")
            .user(user)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        given(likeNumberCacheRepository.getFeedLikeNumber(1L)).willReturn(null);
        given(likeRepository.countByFeedId(1L)).willReturn(0L);
        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(0L);

        //when
        FeedResponse feedResponse = feedService.getOneFeed(2L, 1L);

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).setInitialLikeNumber(0L);
        verify(feedRepository, times(1)).findById(1L);
        assertEquals(feedResponse.getFeedId(), 1L);
    }

    @Test
    @DisplayName("기존 해시태그 찾아내면서 게시물 1개 수정 성공.")
    void successUpdateFeedFoundAlreadySavedHashTag(){
        // given
        String content = "수정 된 게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        List<String> imageUrlListToKeep = new ArrayList<>();
        imageUrlListToKeep.add("existingUrlDto,1");

        User user = User.builder()
            .id(1L)
            .build();

        Hashtag hashtagEntity = Hashtag.builder()
            .id(1L)
            .hashtagString("#potato")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .content(content)
            .imageUrls("urlString,")
            .build();

        FeedHashtagMap feedHashtagMap = FeedHashtagMap.builder()
            .id(1L)
            .feed(feed)
            .hashtag(hashtagEntity)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        doNothing().when(s3Service).deleteImage("urlString");
        given(s3Service.uploadImage(multipartFile)).willReturn("urlString");
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(1L);
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.of(hashtagEntity));
        given(feedHashtagMapRepository.save(any())).willReturn(feedHashtagMap);
        given(feedRepository.save(any())).willReturn(feed);

        // when
        feedService.updateFeed(1L, content, imageFileList, hashtagList,  imageUrlListToKeep, 1L);

        // then
        verify(feedRepository, times(1)).findById(1L);
        verify(s3Service, times(1)).deleteImage(any());
        verify(s3Service, times(1)).uploadImage(any());
        verify(feedHashtagMapRepository, times(1)).deleteAllByFeedId(1L);
        verify(hashtagRepository, times(1)).findByHashtagString(anyString());
        verify(hashtagRepository, times(1)).findByHashtagString(anyString());
        verify(feedRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("새로운 해시태그 저장하면서 게시물 1개 수정 성공.")
    void successUpdateFeedSaveNewHashTag(){
        // given
        String content = "수정 된 게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        List<String> imageUrlListToKeep = new ArrayList<>();
        imageUrlListToKeep.add("existingUrlDto,1");

        User user = User.builder()
            .id(1L)
            .build();

        Hashtag hashtagEntity = Hashtag.builder()
            .id(1L)
            .hashtagString("#potato")
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .content(content)
            .imageUrls("urlString,")
            .build();

        FeedHashtagMap feedHashtagMap = FeedHashtagMap.builder()
            .id(1L)
            .feed(feed)
            .hashtag(hashtagEntity)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        doNothing().when(s3Service).deleteImage("urlString");
        given(s3Service.uploadImage(multipartFile)).willReturn("urlString");
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(1L);
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.empty());
        given(hashtagRepository.save(any())).willReturn(hashtagEntity);
        given(feedHashtagMapRepository.save(any())).willReturn(feedHashtagMap);
        given(feedRepository.save(any())).willReturn(feed);

        // when
        feedService.updateFeed(1L, content, imageFileList, hashtagList, imageUrlListToKeep, 1L);

        // then
        verify(feedRepository, times(1)).findById(1L);
        verify(s3Service, times(1)).deleteImage(any());
        verify(s3Service, times(1)).uploadImage(any());
        verify(feedHashtagMapRepository, times(1)).deleteAllByFeedId(1L);
        verify(hashtagRepository, times(1)).findByHashtagString(anyString());
        verify(hashtagRepository, times(1)).save(any());
        verify(feedHashtagMapRepository, times(1)).save(any());
        verify(feedRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 내용물 텍스트 길이 제한 초과")
    void failedUpdateFeedContentLimitExceed(){
        // given
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<=1001; i++){
            sb.append('c');
        }

        String content = sb.toString();

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, null, 1L)
        );

        // then
        assertEquals(ErrorCode.CONTENT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 해시태그 개수 초과")
    void failedUpdateFeedHashtagCountLimitExceed(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        for(int i=1; i<=11; i++){
            hashtagList.add("#potato");
        }

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, null, 1L)
        );

        // then
        assertEquals(ErrorCode.HASHTAG_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 이미지파일 개수 초과")
    void failedUpdateFeedImagefileCountLimitExceed(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        for(int i=1; i<= 16; i++){
            imageFileList.add(multipartFile);
        }

        List<String> prevImageInfoList = new ArrayList<>();
        for(int i=1; i<= CommonConstant.IMAGE_FILE_COUNT_LIMIT; i++){
            prevImageInfoList.add("imageUrl" + i);
        }

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, prevImageInfoList, 1L)
        );

        // then
        assertEquals(ErrorCode.IMAGE_FILE_COUNT_LIMIT_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 수정 대상 게시물을 찾을 수 없음.")
    void failedUpdateFeedFeedNotFound(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        given(feedRepository.findById(1L)).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, null, 1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 유저 정보와 게시물 작성자 정보가 일치하지 않음.")
    void failedUpdateFeedUserInfoNotMatch(){
        // given
        String content = "게시물 내용";

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("#potato");

        MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

        List<MultipartFile> imageFileList = new ArrayList<>();
        imageFileList.add(multipartFile);

        User user = User.builder()
            .id(2L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, null, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("피드 1개 제거")
    void successDeleteFeed(){
        // given
        User user = User.builder()
            .id(1L)
            .build();

        Rereply rereply = Rereply.builder()
            .id(1L)
            .build();

        Reply reply = Reply.builder()
            .id(1L)
            .rereplyList(new ArrayList<>())
            .build();
        reply.getRereplyList().add(rereply);

        Feed feed = Feed.builder()
            .id(1L)
            .replyList(new ArrayList<>())
            .user(user)
            .imageUrls("imageUrl,")
            .build();
        feed.getReplyList().add(reply);

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        doNothing().when(s3Service).deleteImage(anyString());
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(anyLong());
        doNothing().when(likeNumberCacheRepository).deleteLikeNumberInfo(anyLong());
        doNothing().when(likeRepository).deleteAllByFeedId(anyLong());
        doNothing().when(bookmarkRepository).deleteAllByFeedId(anyLong());
        doNothing().when(rereplyRepository).deleteAllByIdIn(anyList());
        doNothing().when(replyRepository).deleteAllByIdIn(anyList());
        doNothing().when(feedRepository).deleteById(anyLong());

        // when
        feedService.deleteFeed(1L, 1L);

        List<Long> longList = new ArrayList<>();
        longList.add(1L);

        // then
        verify(s3Service, times(1)).deleteImage("imageUrl");
        verify(feedHashtagMapRepository, times(1)).deleteAllByFeedId(1L);
        verify(likeNumberCacheRepository, times(1)).deleteLikeNumberInfo(1L);
        verify(likeRepository, times(1)).deleteAllByFeedId(1L);
        verify(bookmarkRepository, times(1)).deleteAllByFeedId(1L);
        verify(rereplyRepository, times(1)).deleteAllByIdIn(longList);
        verify(replyRepository, times(1)).deleteAllByIdIn(longList);
        verify(feedRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("게시물 삭제 실패 - 삭제 대상 게시물을 찾을 수 없음.")
    void failedDeleteFeedFeedNotFound(){
        // given
        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.deleteFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 삭제 실패 - 유저 정보가 게시물 작성자 정보와 일치하지 않음.")
    void failedDeleteFeedUserInfoNotMatch(){
        // given
        User user = User.builder()
            .id(2L)
            .build();

        Feed feed = Feed.builder()
            .id(1L)
            .user(user)
            .build();

        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.deleteFeed(1L, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

}