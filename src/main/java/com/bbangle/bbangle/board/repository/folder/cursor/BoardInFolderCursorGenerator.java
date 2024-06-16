package com.bbangle.bbangle.board.repository.folder.cursor;

import com.querydsl.core.BooleanBuilder;

public interface BoardInFolderCursorGenerator {

    BooleanBuilder getCursor(Long cursorId, Long folderId);

}
