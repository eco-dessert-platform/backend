package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardCursorGenerator;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MostWishedCursorGenerator implements BoardCursorGenerator {

    private static final QBoard board = QBoard.board;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public BooleanBuilder getCursor(Long cursorId) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }
        Long targetWishCount = Optional.ofNullable(jpaQueryFactory
                .select(boardStatistic.boardWishCount)
                .from(boardStatistic)
                .join(boardStatistic.board, board)
                .where(board.id.eq(cursorId))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        cursorBuilder.and(boardStatistic.boardWishCount.lt(targetWishCount))
            .or(boardStatistic.boardWishCount.eq(targetWishCount).and(board.id.loe(cursorId)));

        return cursorBuilder;
    }

}
