package com.bbangle.bbangle.page;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class BoardCustomPage<T> extends CustomPage<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long boardCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long storeCount;

    public BoardCustomPage(T content, Long requestCursor, Double cursorScore, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

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

    public static BoardCustomPage<List<BoardResponseDto>> emptyPage() {
        long emptyResultNextCursor = -1L;
        boolean emptyResultHasNext = false;

        return from(
            Collections.emptyList(),
            emptyResultNextCursor,
            emptyResultHasNext
        );
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long nextCursor,
        boolean hasNext
    ) {
        return new BoardCustomPage<>(boardList, nextCursor, hasNext);
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext,
        long boardCnt,
        long storeCnt
    ) {
        return new BoardCustomPage<>(boardList, requestCursor, hasNext, boardCnt,
            storeCnt);
    }

}
