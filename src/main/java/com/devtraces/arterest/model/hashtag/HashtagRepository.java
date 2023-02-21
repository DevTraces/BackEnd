package com.devtraces.arterest.model.hashtag;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
	Optional<Hashtag> findByHashtagString(String hashtagString);
}

