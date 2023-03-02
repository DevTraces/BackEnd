package com.devtraces.arterest.model.recommendation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRecommendationRepository extends JpaRepository<LikeRecommendation, Long> {

    Optional<LikeRecommendation> findTopByOrderByIdDesc();

}
