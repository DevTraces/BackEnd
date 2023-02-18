package com.devtraces.arterest.domain.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Boolean existsByEmail(String email);
	Boolean existsByNickname(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findByNickname(String nickname);
	Page<User> findByUsername(String username, Pageable pageable);
	Page<User> findByNickname(String nickname, Pageable pageable);
	Optional<User> findByKakaoUserId(long kakaoUserId);
}
