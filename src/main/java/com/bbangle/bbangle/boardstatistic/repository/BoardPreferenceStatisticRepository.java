package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardPreferenceStatisticRepository extends JpaRepository<BoardPreferenceStatistic, Long>, BoardPreferenceStatisticQueryDSLRepository {

    @Query(value = "select bs from BoardPreferenceStatistic bs where bs.boardId in :boardIds")
    List<BoardPreferenceStatistic> findAllByBoardIds(@Param("boardIds") List<Long> boardIds);

    List<BoardPreferenceStatistic> findAllByBoardId(Long boardId);

}
