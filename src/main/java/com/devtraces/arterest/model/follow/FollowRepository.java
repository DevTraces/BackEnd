package com.devtraces.arterest.model.follow;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByUserIdAndFollowingId(Long user_id, Long followingId);

    Slice<Follow> findAllByFollowingId(Long followingId, PageRequest pageRequest);

    boolean existsByUserIdAndFollowingId(Long user_id, Long followingId);

}
