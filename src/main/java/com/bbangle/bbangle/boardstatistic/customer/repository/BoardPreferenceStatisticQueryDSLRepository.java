package com.bbangle.bbangle.boardstatistic.customer.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import java.util.List;

public interface BoardPreferenceStatisticQueryDSLRepository {

    List<BoardPreferenceStatistic> findUnmatchedBasicScore();
}
