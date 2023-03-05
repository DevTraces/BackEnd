package com.devtraces.arterest.model.rereply;

import com.devtraces.arterest.controller.rereply.dto.response.RereplyResponseConverter;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RereplyRepository extends JpaRepository<Rereply, Long> {

    Slice<Rereply> findAllByReplyId(Long replyId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

    @Query(
        value =
            "SELECT"
                + " rr.id as rereplyId,"
                + " u.profileImageUrl as authorProfileImageUrl, "
                + " u.nickname as authorNickname, "
                + " rr.content as content, "
                + " rr.createdAt as createdAt, "
                + " rr.modifiedAt as modifiedAt "
                + " FROM Rereply rr"
                + " JOIN User u"
                + " ON rr.user.id = u.id"
                + " WHERE rr.reply.id = :inputReplyId "
    )
    Slice<RereplyResponseConverter> findAllRereplyJoinUser(
        @Param("inputReplyId") Long inputReplyId, PageRequest pageRequest
    );

}
