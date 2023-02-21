package com.devtraces.arterest.model.like;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long> {

    // 특정 유저가 좋아요를 누른 피드들의 주키 번호를 찾아내기 위한 쿼리
    // 페이징을 적용할 수 없다.
    // 어떤 게시물을 요청할지 모르는 상황에서 페이징을 통해서 제한된 정보만을 기준으로 판단할 경우,
    // 좋아요 누른 기록을 트루로 할지 펄스로 할지 정확하게 판단할 수 없게 되기 때문이다.
    List<Likes> findAllByUserId(Long userId);

    Long countByFeedId(Long feedId);

    void deleteAllByFeedId(Long feedId);

}
