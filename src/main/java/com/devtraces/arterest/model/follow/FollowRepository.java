package com.devtraces.arterest.model.follow;

import com.devtraces.arterest.model.user.User;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByUserIdAndFollowingId(Long user_id, Long followingId);

    Slice<Follow> findAllByFollowingId(Long followingId, PageRequest pageRequest);

    Integer countAllByFollowingId(Long followingId);

    @Query(
            nativeQuery = true,
            value = "select count(*) " +
                    "from follow f " +
                    "where f.user_id = :userId and f.following_id = :followingId"
    )
    int isFollowing(Long userId, Long followingId);
    
    boolean existsByUserIdAndFollowingId(Long user_id, Long followingId);

    void deleteAllByFollowingId(Long followingId);
    
    void deleteAllByUser(User user);

    Optional<Follow> findTopByOrderByIdDesc();

}
