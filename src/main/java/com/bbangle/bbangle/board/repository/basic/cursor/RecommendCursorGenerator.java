package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGenerator;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendCursorGenerator implements BoardCursorGenerator {

    private static final QRanking ranking = QRanking.ranking;
    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public BooleanBuilder getCursor(Long cursorId) {
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
                .and(board.id.loe(cursorId));

        return cursorBuilder;
    }

}
