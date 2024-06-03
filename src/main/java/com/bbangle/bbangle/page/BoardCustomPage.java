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
    private Double cursorScore;

    public BoardCustomPage(T content, Long requestCursor, Double cursorScore, Boolean hasNext) {
        super(content, requestCursor, hasNext);
        this.cursorScore = cursorScore;
    }

    public BoardCustomPage(
        T content,
        Long requestCursor,
        Double cursorScore,
        Boolean hasNext,
        Long boardCount,
        Long storeCount
    ) {
        super(content, requestCursor, hasNext);
        this.cursorScore = cursorScore;
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
        double emptyResultScore = -1.0;
        boolean emptyResultHasNext = false;

        return from(
            Collections.emptyList(),
            emptyResultNextCursor,
            emptyResultScore,
            emptyResultHasNext
        );
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long nextCursor,
        Double cursorScore,
        boolean hasNext
    ) {
        return new BoardCustomPage<>(boardList, nextCursor, cursorScore, hasNext);
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        Double cursorScore,
        boolean hasNext,
        long boardCnt,
        long storeCnt
    ) {
        return new BoardCustomPage<>(boardList, requestCursor, cursorScore, hasNext, boardCnt,
            storeCnt);
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext
    ) {
        return new BoardCustomPage<>(boardList, requestCursor, hasNext);
    }

}
