package com.devtraces.arterest.model.bookmark;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	Page<Bookmark> findByUserId(Long userId, Pageable pageable);
	void deleteByUserIdAndFeedId(Long userId, Long FeedId);
	List<Bookmark> findAllByUserId(Long user_id);
	void deleteAllByFeedId(Long feed_id);
}
