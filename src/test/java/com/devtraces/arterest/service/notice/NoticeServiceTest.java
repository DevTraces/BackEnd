package com.devtraces.arterest.service.notice;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.exception.ErrorCode;
import com.devtraces.arterest.common.type.NoticeTarget;
import com.devtraces.arterest.common.type.NoticeType;
import com.devtraces.arterest.controller.notice.dto.NumberOfNoticeResponse;
import com.devtraces.arterest.controller.notice.dto.response.NoticeListResponse;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.notice.Notice;
import com.devtraces.arterest.model.notice.NoticeRepository;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.reply.ReplyRepository;
import com.devtraces.arterest.model.rereply.Rereply;
import com.devtraces.arterest.model.rereply.RereplyRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.devtraces.arterest.common.type.NoticeTarget.*;
import static com.devtraces.arterest.common.type.NoticeType.*;
import static com.devtraces.arterest.common.type.NoticeType.REPLY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RereplyRepository rereplyRepository;
    @Mock
    private FollowRepository followRepository;
    @InjectMocks
    private NoticeService noticeService;

    @Test
    void success_createLikeNotice() {
        //given
        Long noticeOwnerId = 1L;
        NoticeType noticeType = LIKE;

        Long sendUserId = 342L;
        User user = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        Long feedId = 9709L;
        User ownerUser = User.builder().id(noticeOwnerId).build();
        Feed feed = Feed.builder().id(feedId).user(ownerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Notice notice = Notice.builder()
                .noticeOwnerId(feed.getUser().getId())
                .user(user)
                .feed(feed)
                .noticeType(noticeType)
                .build();
        given(noticeRepository.save(any())).willReturn(notice);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);

        //when
        noticeService.createLikeNotice(sendUserId, feedId);

        //then
        verify(noticeRepository, times(1)).save(captor.capture());
        assertEquals(noticeOwnerId, captor.getValue().getNoticeOwnerId());
        assertEquals(sendUserId, captor.getValue().getUser().getId());
        assertEquals(feedId, captor.getValue().getFeed().getId());
        assertEquals(LIKE, captor.getValue().getNoticeType());
    }

    @Test
    void success_createFollowNotice() {
        //given
        NoticeType noticeType = FOLLOW;

        Long sendUserId = 342L;
        User user = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        Long noticeOwnerId = 1L;
        String nickname = "ownerNickname";
        User ownerUser = User.builder().id(noticeOwnerId).nickname(nickname).build();
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(ownerUser));

        Notice notice = Notice.builder()
                .noticeOwnerId(ownerUser.getId())
                .user(user)
                .noticeType(FOLLOW)
                .build();
        given(noticeRepository.save(any())).willReturn(notice);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);

        //when
        noticeService.createFollowNotice(nickname, sendUserId);

        //then
        verify(noticeRepository, times(1)).save(captor.capture());
        assertEquals(noticeOwnerId, captor.getValue().getNoticeOwnerId());
        assertEquals(sendUserId, captor.getValue().getUser().getId());
        assertEquals(FOLLOW, captor.getValue().getNoticeType());
    }

    @Test
    void success_createReplyNotice() {
        //given
        Long noticeOwnerId = 1L;
        User ownerUser = User.builder().id(noticeOwnerId).build();

        Long sendUserId = 342L;
        User user = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        Long feedId = 9709L;
        Feed feed = Feed.builder().id(feedId).user(ownerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Long replyId = 103L;
        Reply reply = Reply.builder().id(replyId).build();
        given(replyRepository.findById(anyLong())).willReturn(Optional.of(reply));

        NoticeType noticeType = REPLY;

        Notice notice = Notice.builder()
                .noticeOwnerId(feed.getUser().getId())
                .user(user)
                .feed(feed)
                .noticeType(noticeType)
                .build();
        given(noticeRepository.save(any())).willReturn(notice);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);

        //when
        noticeService.createReplyNotice(sendUserId, feedId, replyId);

        //then
        verify(noticeRepository, times(1)).save(captor.capture());
        assertEquals(noticeOwnerId, captor.getValue().getNoticeOwnerId());
        assertEquals(sendUserId, captor.getValue().getUser().getId());
        assertEquals(feedId, captor.getValue().getFeed().getId());
        assertEquals(replyId, captor.getValue().getReply().getId());
        assertEquals(REPLY, captor.getValue().getNoticeType());
    }

    @Test
    void success_saveNoticeForFeedOwner() {
        //given
        Long feedOwnerId = 1L;
        User feedOwnerUser = User.builder().id(feedOwnerId).build();

        Long replyOwnerId = 2L;
        User replyOwnerUser = User.builder().id(replyOwnerId).build();

        Long sendUserId = 342L;
        User sendUser = User.builder().id(sendUserId).build();

        Long feedId = 9709L;
        Feed feed = Feed.builder().id(feedId).user(feedOwnerUser).build();

        Long replyId = 103L;
        Reply reply = Reply.builder().id(replyId).user(replyOwnerUser).build();

        Long reReplyId = 1231L;
        Rereply reReply = Rereply.builder().id(reReplyId).reply(reply).build();

        NoticeType noticeType = REREPLY;

        Notice noticeForFeedOwner = Notice.builder()
                .noticeOwnerId(feed.getUser().getId())
                .user(sendUser)
                .feed(feed)
                .reply(reply)
                .rereply(reReply)
                .noticeType(noticeType)
                .noticeTarget(POST)
                .build();

        given(noticeRepository.save(any())).willReturn(noticeForFeedOwner);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);

        //when
        noticeService.saveNoticeForFeedOwner(sendUser, feed, reply, reReply);

        //then
        verify(noticeRepository, times(1)).save(captor.capture());
        assertEquals(feedOwnerId, captor.getValue().getNoticeOwnerId());
        assertEquals(sendUserId, captor.getValue().getUser().getId());
        assertEquals(feedId, captor.getValue().getFeed().getId());
        assertEquals(replyId, captor.getValue().getReply().getId());
        assertEquals(POST, captor.getValue().getNoticeTarget());
        assertEquals(REREPLY, captor.getValue().getNoticeType());
    }

    @Test
    void success_saveNoticeForReplyOwner() {
        //given
        Long feedOwnerId = 1L;
        User feedOwnerUser = User.builder().id(feedOwnerId).build();

        Long replyOwnerId = 2L;
        User replyOwnerUser = User.builder().id(replyOwnerId).build();

        Long sendUserId = 342L;
        User sendUser = User.builder().id(sendUserId).build();

        Long feedId = 9709L;
        Feed feed = Feed.builder().id(feedId).user(feedOwnerUser).build();

        Long replyId = 103L;
        Reply reply = Reply.builder().id(replyId).user(replyOwnerUser).build();

        Long reReplyId = 1231L;
        Rereply reReply = Rereply.builder().id(reReplyId).reply(reply).build();

        NoticeType noticeType = REREPLY;

        Notice noticeForReplyOwner = Notice.builder()
                .noticeOwnerId(reply.getUser().getId())
                .user(sendUser)
                .feed(feed)
                .reply(reply)
                .rereply(reReply)
                .noticeType(noticeType)
                .noticeTarget(NoticeTarget.REPLY)
                .build();

        given(noticeRepository.save(any())).willReturn(noticeForReplyOwner);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);

        //when
        noticeService.saveNoticeForReplyOwner(sendUser, feed, reply, reReply);

        //then
        verify(noticeRepository, times(1)).save(captor.capture());
        assertEquals(replyOwnerId, captor.getValue().getNoticeOwnerId());
        assertEquals(sendUserId, captor.getValue().getUser().getId());
        assertEquals(feedId, captor.getValue().getFeed().getId());
        assertEquals(replyId, captor.getValue().getReply().getId());
        assertEquals(NoticeTarget.REPLY, captor.getValue().getNoticeTarget());
        assertEquals(REREPLY, captor.getValue().getNoticeType());
    }

    @Test
    @DisplayName("좋아요 알림 생성 실패 - 존재하지 않는 사용자")
    void fail_createLikeNotice_USER_NOT_FOUND() {
        //given
        Long sendUserId = 35242L;

        Long noticeOwnerId = 1L;
        Long feedId = 2L;
        User ownerUser = User.builder().id(noticeOwnerId).build();
        Feed feed = Feed.builder().id(feedId).user(ownerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> noticeService.createLikeNotice(sendUserId, feedId)
                );

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 알림 생성 실패 - 존재하지 않는 피드")
    void fail_createLikeNotice_FEED_NOT_FOUND() {
        //given
        Long sendUserId = 1L;

        Long feedId = 2L;
        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> noticeService.createLikeNotice(sendUserId, feedId)
                );

        //then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 알림 생성 실패 - 알림 대상 사용자가 존재하지 않음")
    void fail_createFollowNotice_OWNER_USER_NOT_FOUND() {
        //given
        String nickname = "nickname";
        Long sendUserId = 2L;

        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.empty());

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> noticeService.createFollowNotice(nickname, sendUserId)
                );

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 알림 생성 실패 - 알림 보내는 사용자가 존재하지 않음")
    void fail_createFollowNotice_SEND_USER_NOT_FOUND() {
        //given
        String nickname = "nickname";
        User ownerUser = User.builder().nickname(nickname).build();
        given(userRepository.findByNickname(anyString()))
                .willReturn(Optional.of(ownerUser));

        Long sendUserId = 342L;

        //when
        BaseException exception =
                assertThrows(
                        BaseException.class,
                        () -> noticeService.createFollowNotice(nickname, sendUserId)
                );

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 알림 생성 실패 - 존재하지 않는 피드")
    void fail_createReplyNotice_FEED_NOT_FOUND() {
        //given
        Long sendUserId = 342L;
        Long feedId = 9709L;
        Long replyId = 103L;

        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReplyNotice(sendUserId, feedId, replyId)
        );

        //then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 알림 생성 실패 - 존재하지 않는 댓글 작성자")
    void fail_createReplyNotice_SEND_USER_NOT_FOUND() {
        //given
        Long noticeOwnerId = 1L;
        User ownerUser = User.builder().id(noticeOwnerId).build();

        Long sendUserId = 342L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        Long feedId = 9709L;
        Feed feed = Feed.builder().id(feedId).user(ownerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Long replyId = 103L;

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReplyNotice(sendUserId, feedId, replyId)
        );

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 알림 생성 실패 - 존재하지 않는 댓글")
    void fail_createReplyNotice_REPLY_NOT_FOUND() {
        //given
        Long noticeOwnerId = 1L;
        User ownerUser = User.builder().id(noticeOwnerId).build();

        Long sendUserId = 342L;
        User user = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        Long feedId = 9709L;
        Feed feed = Feed.builder().id(feedId).user(ownerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Long replyId = 103L;
        given(replyRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReplyNotice(sendUserId, feedId, replyId)
        );

        //then
        assertEquals(ErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 알림 생성 실패 - 존재하지 않는 사용자")
    void fail_createReReplyNotice_USER_NOT_FOUND() {
        //given
        Long sendUserId = 1L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        Long feedId = 2L;
        Long replyId = 3L;
        Long reReplyId = 4L;

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReReplyNotice(
                        sendUserId, feedId, replyId, reReplyId
                )
        );

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 알림 생성 실패 - 존재하지 않는 피드")
    void fail_createReReplyNotice_FEED_NOT_FOUND() {
        //given
        Long sendUserId = 1L;
        User sendUser = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(sendUser));

        Long feedId = 2L;
        given(feedRepository.findById(anyLong())).willReturn(Optional.empty());

        Long replyId = 3L;
        Long reReplyId = 4L;

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReReplyNotice(
                        sendUserId, feedId, replyId, reReplyId
                )
        );

        //then
        assertEquals(ErrorCode.FEED_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 알림 생성 실패 - 존재하지 않는 댓글")
    void fail_createReReplyNotice_REPLY_NOT_FOUND() {
        //given
        Long sendUserId = 1L;
        User sendUser = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(sendUser));

        Long feedOwnerId = 1L;
        User feedOwnerUser = User.builder().id(feedOwnerId).build();

        Long feedId = 2L;
        Feed feed = Feed.builder().id(feedId).user(feedOwnerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Long replyId = 3L;
        given(replyRepository.findById(anyLong())).willReturn(Optional.empty());

        Long reReplyId = 4L;

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReReplyNotice(
                        sendUserId, feedId, replyId, reReplyId
                )
        );

        //then
        assertEquals(ErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 알림 생성 실패 - 존재하지 않는 대댓글")
    void fail_createReReplyNotice_REREPLY_NOT_FOUND() {
        //given
        Long sendUserId = 74L;
        User sendUser = User.builder().id(sendUserId).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(sendUser));

        Long feedOwnerId = 2342L;
        User feedOwnerUser = User.builder().id(feedOwnerId).build();

        Long feedId = 642L;
        Feed feed = Feed.builder().id(feedId).user(feedOwnerUser).build();
        given(feedRepository.findById(anyLong())).willReturn(Optional.of(feed));

        Long replyOwnerId = 3151L;
        User replyOwnerUser = User.builder().id(replyOwnerId).build();

        Long replyId = 351L;
        Reply reply = Reply.builder().id(replyId).user(replyOwnerUser).build();
        given(replyRepository.findById(anyLong())).willReturn(Optional.of(reply));

        Long reReplyId = 135L;
        given(rereplyRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        BaseException exception = assertThrows(
                BaseException.class,
                () -> noticeService.createReReplyNotice(
                        sendUserId, feedId, replyId, reReplyId
                )
        );

        //then
        assertEquals(ErrorCode.REREPLY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void success_getNumberOfNotice() {
        //given
        Long noticeOwnerId = 1L;

        Integer numberOfNotice = 10;
        given(noticeRepository.countAllByNoticeOwnerId(anyLong()))
                .willReturn(numberOfNotice);

        //when
        NumberOfNoticeResponse response =
                noticeService.getNumberOfNotice(noticeOwnerId);

        //then
        assertEquals(numberOfNotice, response.getNoticeNumber());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 좋아요 - 내 게시물에 좋아요 누름")
    void success_getNoticeList_LIKE() {
        //given
        Long noticeOwnerId = 1L;
        User noticeOwner = User.builder()
                .id(noticeOwnerId)
                .build();

        Integer numberOfNotices = 1;

        User likeOwner = User.builder()
                .id(325L)
                .nickname("likeNickname")
                .profileImageUrl("likeProfileImageUrl")
                .build();
        Feed feed = Feed.builder()
                .id(32409L)
                .user(noticeOwner)
                .imageUrls("first, second, third")
                .content("feedContent")
                .build();

        List<Notice> noticeList = new ArrayList<>();
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwner.getId())
                .user(likeOwner)
                .feed(feed)
                .noticeType(LIKE)
                .createdAt(LocalDateTime.now())
                .build());

        PageImpl<Notice> notices = new PageImpl<>(noticeList);

        int page = 1;
        int pageSize = 10;
        given(noticeRepository.findALlByNoticeOwnerId(anyLong(), any()))
                .willReturn(notices);

        //when
        List<NoticeListResponse> responseList =
                noticeService.getNoticeList(noticeOwnerId, page, pageSize);

        //then
        assertEquals(numberOfNotices, responseList.size());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 팔로우 - 내가 팔로우하는 사람이 팔로우 요청")
    void success_getNoticeList_FOLLOW_ISFOLLOWING_TRUE() {
        //given
        Long noticeOwnerId = 1L;

        Integer numberOfNotices = 1;

        User followerUser = User.builder()
                .id(325L)
                .nickname("followerNickname")
                .profileImageUrl("followerProfileImageUrl")
                .build();

        given(followRepository.isFollowing(anyLong(), anyLong()))
                .willReturn(1);

        List<Notice> noticeList = new ArrayList<>();
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwnerId)
                .user(followerUser)
                .noticeType(FOLLOW)
                .createdAt(LocalDateTime.now())
                .build());

        int page = 1;
        int pageSize = 10;
        PageImpl<Notice> notices = new PageImpl<>(noticeList);
        given(noticeRepository.findALlByNoticeOwnerId(anyLong(), any()))
                .willReturn(notices);

        //when
        List<NoticeListResponse> responseList =
                noticeService.getNoticeList(noticeOwnerId, page, pageSize);

        //then
        assertEquals(numberOfNotices, responseList.size());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 팔로우 - 내가 팔로우하지 않는 사람이 팔로우 요청")
    void success_getNoticeList_FOLLOW_ISFOLLOWING_FALSE() {
        //given
        Long noticeOwnerId = 1L;

        Integer numberOfNotices = 1;

        User followerUser = User.builder()
                .id(325L)
                .nickname("followerNickname")
                .profileImageUrl("followerProfileImageUrl")
                .build();

        given(followRepository.isFollowing(anyLong(), anyLong()))
                .willReturn(0);

        List<Notice> noticeList = new ArrayList<>();
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwnerId)
                .user(followerUser)
                .noticeType(FOLLOW)
                .createdAt(LocalDateTime.now())
                .build());

        int page = 1;
        int pageSize = 10;
        PageImpl<Notice> notices = new PageImpl<>(noticeList);
        given(noticeRepository.findALlByNoticeOwnerId(anyLong(), any()))
                .willReturn(notices);

        //when
        List<NoticeListResponse> responseList =
                noticeService.getNoticeList(noticeOwnerId, page, pageSize);

        //then
        assertEquals(numberOfNotices, responseList.size());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 댓글 - 내가 작성한 피드에 댓글 추가됨")
    void success_getNoticeList_REPLY() {
        //given
        Long noticeOwnerId = 1L;
        User noticeOwner = User.builder()
                .id(noticeOwnerId)
                .build();

        Integer numberOfNotices = 1;

        User replyOwner = User.builder()
                .id(325L)
                .nickname("replyNickname")
                .profileImageUrl("replyProfileImageUrl")
                .build();
        Feed feed = Feed.builder()
                .id(32409L)
                .user(noticeOwner)
                .imageUrls("first, second, third")
                .content("feedContent")
                .build();
        Reply reply = Reply.builder()
                .id(34792L)
                .user(replyOwner)
                .feed(feed)
                .content("replyContent")
                .build();

        List<Notice> noticeList = new ArrayList<>();
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwnerId)
                .user(replyOwner)
                .feed(feed)
                .reply(reply)
                .noticeType(REPLY)
                .createdAt(LocalDateTime.now())
                .build());

        int page = 1;
        int pageSize = 10;
        PageImpl<Notice> notices = new PageImpl<>(noticeList);
        given(noticeRepository.findALlByNoticeOwnerId(anyLong(), any()))
                .willReturn(notices);

        //when
        List<NoticeListResponse> responseList =
                noticeService.getNoticeList(noticeOwnerId, page, pageSize);

        //then
        assertEquals(numberOfNotices, responseList.size());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 대댓글 - 내가 작성한 피드 혹은 댓글에 대댓글 추가됨")
    void success_getNoticeList_REREPLY() {
        //given
        Long noticeOwnerId = 1L;
        User feedOwner = User.builder()
                .id(noticeOwnerId)
                .build();

        Integer numberOfNotices = 2;

        User replyOwner = User.builder()
                .id(325L)
                .nickname("replyNickname")
                .profileImageUrl("replyProfileImageUrl")
                .build();
        User rereplyOwner = User.builder().id(9383L)
                .nickname("rereplyNickname")
                .profileImageUrl("rereplyProfileImageUrl")
                .build();

        Feed feed = Feed.builder()
                .id(32409L)
                .user(feedOwner)
                .imageUrls("first, second, third")
                .content("feedContent")
                .build();
        Reply reply = Reply.builder()
                .id(34792L)
                .user(replyOwner)
                .feed(feed)
                .content("replyContent")
                .build();
        Rereply rereply = Rereply.builder()
                .id(483729L)
                .user(rereplyOwner)
                .reply(reply)
                .content("rereplyContent")
                .build();

        List<Notice> noticeList = new ArrayList<>();
        LocalDateTime createdAt = LocalDateTime.now();
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwnerId)
                .user(feedOwner)
                .feed(feed)
                .reply(reply)
                .rereply(rereply)
                .noticeType(REREPLY)
                .noticeTarget(POST)
                .createdAt(createdAt)
                .build());
        noticeList.add(Notice.builder()
                .noticeOwnerId(noticeOwnerId)
                .user(replyOwner)
                .feed(feed)
                .reply(reply)
                .rereply(rereply)
                .noticeType(REREPLY)
                .noticeTarget(NoticeTarget.REPLY)
                .createdAt(createdAt)
                .build());

        int page = 1;
        int pageSize = 10;
        PageImpl<Notice> notices = new PageImpl<>(noticeList);
        given(noticeRepository.findALlByNoticeOwnerId(anyLong(), any()))
                .willReturn(notices);

        //when
        List<NoticeListResponse> responseList =
                noticeService.getNoticeList(noticeOwnerId, page, pageSize);

        //then
        assertEquals(numberOfNotices, responseList.size());
    }
}