package com.bbangle.bbangle.board.repository.folder.cursor;

import com.querydsl.core.BooleanBuilder;

public interface BoardCursorGenerator {

    BooleanBuilder getCursor(Long cursorId);

}
