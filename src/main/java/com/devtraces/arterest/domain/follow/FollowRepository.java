package com.devtraces.arterest.domain.follow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByUserIdAndFollowingId(Long user_id, Long followingId);

}
