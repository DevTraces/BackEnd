package com.devtraces.arterest.model.like;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long> {

    // 특정 유저가 좋아요를 누른 피드들의 주키 번호를 찾아내기 위한 쿼리
    List<Likes> findAllByUserId(Long userId);

    Slice<Likes> findAllByFeedIdOrderByCreatedAtDesc(Long feedId, PageRequest pageRequest);

    Long countByFeedId(Long feedId);

    void deleteAllByFeedId(Long feedId);

    boolean existsByUserIdAndFeedId(Long userId, Long feedId);

    void deleteByUserIdAndFeedId(Long userId, Long feedId);

}
