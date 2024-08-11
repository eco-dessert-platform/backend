package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardPreferenceStatisticRepository extends JpaRepository<BoardPreferenceStatistic, Long>, BoardPreferenceStatisticQueryDSLRepository {

}
