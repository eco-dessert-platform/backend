package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import org.springframework.data.repository.CrudRepository;

public interface RecommendationSimilarBoardRepository extends
    CrudRepository<RecommendationSimilarBoard, Long> {

}
