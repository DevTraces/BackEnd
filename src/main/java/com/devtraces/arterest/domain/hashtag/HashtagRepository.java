package com.devtraces.arterest.domain.hashtag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
	Page<Hashtag> findByHashtag(String hashtag, Pageable pageable);
}
