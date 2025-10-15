package com.bbangle.bbangle.boardstatistic.customer.repository;

import java.util.List;

public interface BoardStatisticQueryDSLRepository {

    List<Long> findPopularBoardIds(int limit);
}
