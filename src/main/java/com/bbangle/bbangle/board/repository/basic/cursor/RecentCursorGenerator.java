package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.CursorGenerator;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecentCursorGenerator implements CursorGenerator {

    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory jpaQueryFactory;
    private final Long cursorId;

    @Override
    public BooleanBuilder getCursor() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }

        Long validatedCursorId = Optional.ofNullable(jpaQueryFactory.select(board.id)
                .from(board)
                .where(board.id.eq(cursorId))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        cursorBuilder.and(board.id.loe(validatedCursorId));

        return cursorBuilder;
    }

}
