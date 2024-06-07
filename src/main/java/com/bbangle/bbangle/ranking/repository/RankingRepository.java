package com.bbangle.bbangle.ranking.repository;

import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingRepository extends JpaRepository<BoardStatistic, Long> {

    Optional<BoardStatistic> findByBoardId(Long boardId);

}
