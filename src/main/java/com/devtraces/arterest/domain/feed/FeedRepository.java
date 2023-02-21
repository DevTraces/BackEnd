package com.devtraces.arterest.domain.feed;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findAllByUserId(Long authorId, PageRequest pageRequest);

    Integer countAllByUserId(Long userId);

}
