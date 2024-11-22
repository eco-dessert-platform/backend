package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import java.util.List;

public interface BoardPreferenceStatisticQueryDSLRepository {

    List<BoardPreferenceStatistic> findUnmatchedBasicScore();
}
