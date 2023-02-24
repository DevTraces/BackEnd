package com.devtraces.arterest.model.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
	Page<User> findByUsernameStartsWith(String username, Pageable pageable);
	Page<User> findByNicknameStartsWith(String nickname, Pageable pageable);
	Optional<User> findByKakaoUserId(long kakaoUserId);

	Slice<User> findAllByIdIn(Collection<Long> idList, PageRequest pageRequest);

	List<User> findAllByIdIn(Collection<Long> id);
}
