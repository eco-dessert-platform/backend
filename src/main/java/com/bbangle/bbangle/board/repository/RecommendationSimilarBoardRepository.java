package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationSimilarBoardRepository extends
    JpaRepository<RecommendationSimilarBoard, Long> {

}
