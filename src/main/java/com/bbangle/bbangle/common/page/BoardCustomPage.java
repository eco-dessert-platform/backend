package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.board.dto.BoardResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardCustomPage<T> extends CustomPage<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long boardCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long storeCount;

    public BoardCustomPage(
        T content,
        Long requestCursor,
        Boolean hasNext,
        Long boardCount,
        Long storeCount
    ) {
        super(content, requestCursor, hasNext);
        this.boardCount = boardCount;
        this.storeCount = storeCount;
    }

    public BoardCustomPage(
        T content,
        Long requestCursor,
        Boolean hasNext
    ) {
        super(content, requestCursor, hasNext);
    }

    public static BoardCustomPage<List<BoardResponse>> emptyPage() {
        long emptyResultNextCursor = -1L;
        boolean emptyResultHasNext = false;

        return from(
            Collections.emptyList(),
            emptyResultNextCursor,
            emptyResultHasNext
        );
    }

    public static BoardCustomPage<List<BoardResponse>> from(
        List<BoardResponse> boardList,
        Long nextCursor,
        boolean hasNext
    ) {
        return new BoardCustomPage<>(boardList, nextCursor, hasNext);
    }
}
