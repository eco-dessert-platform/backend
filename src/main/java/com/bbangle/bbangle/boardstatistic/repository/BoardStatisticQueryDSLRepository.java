package com.bbangle.bbangle.boardstatistic.repository;

import java.util.List;

public interface BoardStatisticQueryDSLRepository {

    List<Long> findPopularBoardIds(int limit);
}
