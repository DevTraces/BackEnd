package com.devtraces.arterest.model.feedhashtagmap;

import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.hashtag.Hashtag;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedHashtagMapRepository extends JpaRepository<FeedHashtagMap, Long> {
	void deleteAllByFeedId(Long feed_id);
	List<FeedHashtagMap> findByFeed(Feed feed);
	boolean existsByHashtag(Hashtag hashtag);
}

