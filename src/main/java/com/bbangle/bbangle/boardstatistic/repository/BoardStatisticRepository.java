package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardStatisticRepository extends JpaRepository<BoardStatistic, Long> {

    Optional<BoardStatistic> findByBoardId(Long boardId);

}
