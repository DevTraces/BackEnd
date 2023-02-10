package com.devtraces.arterest.domain.feed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedRepository extends JpaRepository<Feed, Long> {
	@Query(value = "SELECT f.hashtags" +
	"FROM FEED f", nativeQuery = true)
	List<FeedHashtagsInterface> findAllFeedHashtags();
}
