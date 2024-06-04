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
public class LowPriceCursorGenerator implements CursorGenerator {

    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory jpaQueryFactory;
    private final Long cursorId;

    @Override
    public BooleanBuilder getCursor() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();

        int targetPrice = Optional.ofNullable(jpaQueryFactory.select(board.price)
                .from(board)
                .where(board.id.eq(cursorId))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        cursorBuilder.and(board.price.goe(targetPrice).and(board.id.loe(cursorId)));

        return cursorBuilder;
    }

}
