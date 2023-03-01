package com.devtraces.arterest.model.feed;

import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findAllByUserIdOrderByCreatedAtDesc(Long authorId, PageRequest pageRequest);

    Integer countAllByUserId(Long userId);

    Slice<Feed> findAllByUserIdInAndCreatedAtBetweenOrderByCreatedAtDesc(
        Collection<Long> idList, LocalDateTime from, LocalDateTime to, PageRequest pageRequest
    );

    Slice<Feed> findAllByIdInOrderByCreatedAtDesc(Collection<Long> id);

}
