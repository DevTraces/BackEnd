package com.devtraces.arterest.service.feed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devtraces.arterest.common.component.S3Util;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.controller.feed.dto.FeedResponse;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.feed.FeedRepository;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMapRepository;
import com.devtraces.arterest.domain.hashtag.Hashtag;
import com.devtraces.arterest.domain.hashtag.HashtagRepository;
import com.devtraces.arterest.domain.like.LikeRepository;
import com.devtraces.arterest.domain.likecache.LikeNumberCacheRepository;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.reply.ReplyRepository;
import com.devtraces.arterest.domain.rereply.Rereply;
import com.devtraces.arterest.domain.rereply.RereplyRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
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
    private ReplyRepository replyrepository;
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
    private S3Util s3Util;
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
        given(s3Util.uploadImage(multipartFile)).willReturn("urlString");
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.of(hashtagEntity));
        given(feedRepository.save(any())).willReturn(feed);

        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(1L);

        // when
        feedService.createFeed(1L, content, imageFileList, hashtagList);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(s3Util, times(1)).uploadImage(any());
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
        given(s3Util.uploadImage(multipartFile)).willReturn("urlString");
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.empty());
        given(hashtagRepository.save(any())).willReturn(hashtagEntity);
        given(feedRepository.save(any())).willReturn(feed);

        doNothing().when(likeNumberCacheRepository).setInitialLikeNumber(1L);

        // when
        feedService.createFeed(1L, content, imageFileList, hashtagList);

        // then
        verify(userRepository, times(1)).findById(anyLong());
        verify(s3Util, times(1)).uploadImage(any());
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
        List<FeedResponse> feedResponseList = feedService.getFeedResponseList(1L, PageRequest.of(0, 10));

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

        //when
        List<FeedResponse> feedResponseList = feedService.getFeedResponseList(1L, PageRequest.of(0, 10));

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
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

        //when
        FeedResponse feedResponse = feedService.getOneFeed(2L, 1L);

        //then
        verify(likeNumberCacheRepository, times(1)).getFeedLikeNumber(1L);
        verify(likeRepository, times(1)).countByFeedId(1L);
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
        doNothing().when(s3Util).deleteImage("urlString");
        given(s3Util.uploadImage(multipartFile)).willReturn("urlString");
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(1L);
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.of(hashtagEntity));
        given(feedHashtagMapRepository.save(any())).willReturn(feedHashtagMap);
        given(feedRepository.save(any())).willReturn(feed);

        // when
        feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L);

        // then
        verify(feedRepository, times(1)).findById(1L);
        verify(s3Util, times(1)).deleteImage(any());
        verify(s3Util, times(1)).uploadImage(any());
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
        doNothing().when(s3Util).deleteImage("urlString");
        given(s3Util.uploadImage(multipartFile)).willReturn("urlString");
        doNothing().when(feedHashtagMapRepository).deleteAllByFeedId(1L);
        given(hashtagRepository.findByHashtagString("#potato")).willReturn(Optional.empty());
        given(hashtagRepository.save(any())).willReturn(hashtagEntity);
        given(feedHashtagMapRepository.save(any())).willReturn(feedHashtagMap);
        given(feedRepository.save(any())).willReturn(feed);

        // when
        feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L);

        // then
        verify(feedRepository, times(1)).findById(1L);
        verify(s3Util, times(1)).deleteImage(any());
        verify(s3Util, times(1)).uploadImage(any());
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
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L)
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
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L)
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

        // when
        BaseException exception = assertThrows(
            BaseException.class ,
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L)
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
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L)
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
            () -> feedService.updateFeed(1L, content, imageFileList, hashtagList, 1L)
        );

        // then
        assertEquals(ErrorCode.USER_INFO_NOT_MATCH, exception.getErrorCode());
    }

}



















