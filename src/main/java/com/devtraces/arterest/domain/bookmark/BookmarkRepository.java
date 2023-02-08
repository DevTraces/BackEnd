package com.devtraces.arterest.domain.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	Page<Bookmark> findByUserId(Long userId, Pageable pageable);
	void deleteByUserIdAndFeedId(Long userId, Long FeedId);
}
