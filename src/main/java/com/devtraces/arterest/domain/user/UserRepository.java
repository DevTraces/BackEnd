package com.devtraces.arterest.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findByNickname(String nickname);

	Optional<User> findByKakaoUserId(long kakaoUserId);
}
