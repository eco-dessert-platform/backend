package com.bbangle.bbangle.board.recommend.repository;

import com.bbangle.bbangle.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendBoardRepository extends JpaRepository<Board, Long>, RecommendBoardQueryDSLRepository {

}
