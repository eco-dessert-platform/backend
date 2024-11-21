package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendBoardRepository extends JpaRepository<RecommendationSimilarBoard, Long> {

}
