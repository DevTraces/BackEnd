package com.devtraces.arterest.domain.hashtag;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
	Optional<Hashtag> findByHashtagString(String hashtagString);
}

