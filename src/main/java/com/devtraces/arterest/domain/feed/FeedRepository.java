package com.devtraces.arterest.domain.feed;

import com.devtraces.arterest.domain.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findAllByUserId(Long authorId, PageRequest pageRequest);

}
