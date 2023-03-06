package com.devtraces.arterest.model.reply;

import com.devtraces.arterest.controller.feed.dto.response.FeedResponseConverter;
import com.devtraces.arterest.controller.reply.dto.response.ReplyResponseConverter;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Slice<Reply> findAllByFeedIdOrderByCreatedAtDesc(Long feedId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

    @Query(
        value =
            "SELECT"
                + " r.id as replyId,"
                + " u.profileImageUrl as authorProfileImageUrl, "
                + " u.nickname as authorNickname, "
                + " r.content as content, "
                + " r.numberOfRereplies as numberOfRereply, "
                + " r.createdAt as createdAt, "
                + " r.modifiedAt as modifiedAt "
                + " FROM Reply r"
                + " JOIN User u"
                + " ON r.user.id = u.id"
                + " WHERE r.feed.id = :inputFeedIdId "
                + " ORDER BY r.createdAt DESC "
    )
    Slice<ReplyResponseConverter> findAllReplyJoinUserLatestFirst(
        @Param("inputFeedIdId") Long inputFeedIdId, PageRequest pageRequest
    );

}
