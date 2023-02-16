package com.devtraces.arterest.domain.feedhashtagmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedHashtagMapRepository extends JpaRepository<FeedHashtagMap, Long> {
	void deleteAllByFeedId(Long feed_id);
}

