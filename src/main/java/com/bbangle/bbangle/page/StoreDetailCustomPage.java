package com.bbangle.bbangle.page;

import com.bbangle.bbangle.board.dto.BoardInfoDto;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class StoreDetailCustomPage<T> extends CustomPage<T> {

    public StoreDetailCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static StoreDetailCustomPage<List<BoardInfoDto>> from(
        List<BoardInfoDto> tagCategories,
        List<Long> boardIds,
        Boolean hasNext
    ) {
        Long cursorId = boardIds.get(boardIds.size() - 1);
        return new StoreDetailCustomPage<>(tagCategories, cursorId, hasNext);
    }

    public static StoreDetailCustomPage<List<BoardInfoDto>> empty(Long cursorId) {
        return new StoreDetailCustomPage<>(Collections.emptyList(), cursorId, false);
    }

}