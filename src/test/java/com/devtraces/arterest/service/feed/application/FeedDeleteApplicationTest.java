package com.devtraces.arterest.service.feed.application;

import static org.junit.jupiter.api.Assertions.*;

import com.devtraces.arterest.service.bookmark.BookmarkService;
import com.devtraces.arterest.service.feed.FeedDeleteService;
import com.devtraces.arterest.service.feed.FeedReadService;
import com.devtraces.arterest.service.hashtag.HashtagService;
import com.devtraces.arterest.service.like.LikeService;
import com.devtraces.arterest.service.reply.ReplyService;
import com.devtraces.arterest.service.rereply.RereplyService;
import com.devtraces.arterest.service.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedDeleteApplicationTest {

    @Mock
    private FeedDeleteService feedDeleteService;
    @Mock
    private FeedReadService feedReadService;
    @Mock
    private S3Service s3Service;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private LikeService likeService;
    @Mock
    private BookmarkService bookmarkService;
    @Mock
    private RereplyService rereplyService;
    @Mock
    private ReplyService replyService;
    @InjectMocks
    private FeedDeleteApplication feedDeleteApplication;

    @Test
    @DisplayName("게시물 1개 삭제 성공")
    void successDeleteFeed(){
        // given

        // when

        // then

    }

    @Test
    @DisplayName("게시물 1개 삭제 실패 - 유저 정보 불일치")
    void failedDeleteFeedUserInfoNotMatch(){
        // given

        // when

        // then

    }

}