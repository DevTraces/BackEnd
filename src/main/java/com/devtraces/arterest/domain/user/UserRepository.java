package com.devtraces.arterest.domain.user;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findByNickname(String nickname);

	Optional<User> findByKakaoUserId(long kakaoUserId);

	Slice<User> findAllByIdIn(Collection<Long> idList, PageRequest pageRequest);
}
