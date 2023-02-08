package com.devtraces.arterest.domain.feed;

import com.devtraces.arterest.domain.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    // 이걸로 찾는게 되나??
    Slice<Feed> findAllByAuthorId(Long authorId, PageRequest pageRequest);

}
