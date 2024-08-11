package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardPreferenceStatisticRepositoryImpl implements BoardPreferenceStatisticQueryDSLRepository{

    private static final QBoardPreferenceStatistic preferenceStatistic = QBoardPreferenceStatistic.boardPreferenceStatistic;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardPreferenceStatistic> findUnmatchedBasicScore() {
        return queryFactory.select(preferenceStatistic)
            .from(preferenceStatistic)
            .join(boardStatistic)
            .on(boardStatistic.boardId.eq(preferenceStatistic.boardId))
            .where(boardStatistic.basicScore.ne(preferenceStatistic.basicScore))
            .fetch();
    }

}
