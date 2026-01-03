package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

    @EntityGraph(attributePaths = {"store", "boardDetail", "boardStatistic"})
    Optional<Board> findById(Long boardId);

    @EntityGraph(attributePaths = {"boardStatistic"})
    Page<Board> findByIsCrawlingTrueAndIsDeletedFalse(Pageable pageable);

}
