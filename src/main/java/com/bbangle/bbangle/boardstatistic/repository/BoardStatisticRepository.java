package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.customer.repository.BoardStatisticQueryDSLRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardStatisticRepository extends JpaRepository<BoardStatistic, Long>,
    BoardStatisticQueryDSLRepository {

    Optional<BoardStatistic> findByBoardId(Long boardId);

    @Query(value = "select bs from BoardStatistic bs join bs.board b where b.id in :boardIds")
    List<BoardStatistic> findAllByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Query(value = "select bs.boardReviewGrade from BoardStatistic bs join bs.board b where b.id = :boardId")
    BigDecimal findBoardReviewGradeByBoardId(@Param("boardId") Long boardId);
}
