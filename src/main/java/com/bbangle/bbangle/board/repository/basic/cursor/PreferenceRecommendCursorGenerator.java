package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardCursorGenerator;
import com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreferenceRecommendCursorGenerator{

    private static final QBoardPreferenceStatistic boardStatistic = QBoardPreferenceStatistic.boardPreferenceStatistic;
    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory jpaQueryFactory;

    public BooleanBuilder getCursor(Long cursorId, PreferenceType preferenceType) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        cursorBuilder.and(board.isDeleted.eq(false));
        if (cursorId == null) {
            return cursorBuilder;
        }

        Double targetScore = Optional.ofNullable(jpaQueryFactory.select(boardStatistic.preferenceScore)
                .from(boardStatistic)
                .where(boardStatistic.boardId.eq(cursorId).and(boardStatistic.preferenceType.eq(preferenceType)))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(
                BbangleErrorCode.RANKING_NOT_FOUND));

        cursorBuilder.and(boardStatistic.preferenceScore.lt(targetScore).and(boardStatistic.preferenceType.eq(preferenceType)))
                .or(boardStatistic.preferenceScore.eq(targetScore).and(board.id.loe(cursorId)).and(boardStatistic.preferenceType.eq(preferenceType)));

        return cursorBuilder;
    }

}
