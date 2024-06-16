package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

}
