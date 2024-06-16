package com.bbangle.bbangle.page;

import com.bbangle.bbangle.store.dto.BoardsInStoreResponse;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class StoreDetailCustomPage<T> extends CustomPage<T> {

    public StoreDetailCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static StoreDetailCustomPage<List<BoardsInStoreResponse>> from(
        List<BoardsInStoreResponse> boardsInStoreResponse,
        List<Long> boardIds,
        Boolean hasNext
    ) {
        Long cursorId = boardIds.get(boardIds.size() - 1);
        return new StoreDetailCustomPage<>(boardsInStoreResponse, cursorId, hasNext);
    }

    public static StoreDetailCustomPage<List<BoardsInStoreResponse>> empty(Long cursorId) {
        return new StoreDetailCustomPage<>(Collections.emptyList(), cursorId, false);
    }

}