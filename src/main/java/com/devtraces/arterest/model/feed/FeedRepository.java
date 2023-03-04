package com.devtraces.arterest.model.feed;

import com.devtraces.arterest.model.converter.FeedResponseConverter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findAllByUserIdOrderByCreatedAtDesc(Long authorId, PageRequest pageRequest);

    Integer countAllByUserId(Long userId);

    Slice<Feed> findAllByUserIdInAndCreatedAtBetweenOrderByCreatedAtDesc(
        Collection<Long> idList, LocalDateTime from, LocalDateTime to, PageRequest pageRequest
    );

    Integer countAllByUserIdInAndCreatedAtBetween(
        Collection<Long> idList, LocalDateTime from, LocalDateTime to
    );

    Slice<Feed> findAllByIdInOrderByCreatedAtDesc(Collection<Long> id, PageRequest pageRequest);

    @Query(
        value =
            "SELECT"
                + " f.id as feedId,"
                + " u.profileImageUrl as authorProfileImageUrl, "
                + " u.nickname as authorNickname, "
                + " f.content as content, "
                + " f.imageUrls as imageUrls, "
                + " f.hashtagStringValues as hashtags, "
                + " f.numberOfReplies as numberOfReply, "
                + " f.createdAt as createdAt, "
                + " f.modifiedAt as modifiedAt "
                + " FROM Feed f"
                + " JOIN User u"
                + " ON f.user.id = u.id"
                + " WHERE f.id = :inputFeedId "
    )
    Optional<FeedResponseConverter> findOneFeedJoinUser(
        @Param("inputFeedId") Long inputFeedId
    );

}
