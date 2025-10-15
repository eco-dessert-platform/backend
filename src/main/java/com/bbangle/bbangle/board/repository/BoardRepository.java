package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.customer.repository.BoardQueryDSLRepository;
import com.bbangle.bbangle.board.domain.Board;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

    @EntityGraph(attributePaths = {"store", "boardDetail", "boardStatistic"})
    Optional<Board> findById(Long boardId);

}
