package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.repository.folder.cursor.CursorGenerator;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MostWishedCursorGenerator implements CursorGenerator {

    private final JPAQueryFactory jpaQueryFactory;
    private final Long cursorId;

    @Override
    public BooleanBuilder getCursor() {
        return null;
    }

}
