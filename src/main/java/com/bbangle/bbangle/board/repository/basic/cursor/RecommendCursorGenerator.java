package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.CursorGenerator;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecommendCursorGenerator implements CursorGenerator {

    private static final QRanking ranking = QRanking.ranking;
    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory jpaQueryFactory;
    private final Long cursorId;

    @Override
    public BooleanBuilder getCursor() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }

        Double targetScore = Optional.ofNullable(jpaQueryFactory.select(ranking.recommendScore)
                .from(ranking)
                .where(ranking.board.id.eq(cursorId))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(
                BbangleErrorCode.RANKING_NOT_FOUND));

        cursorBuilder.and(ranking.recommendScore.loe(targetScore))
            .or(ranking.popularScore.eq(targetScore)
                .and(board.id.loe(cursorId)));

        return cursorBuilder;

    }

}