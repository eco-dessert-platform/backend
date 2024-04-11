package com.bbangle.bbangle.page;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;

@Getter
public class BoardCustomPage<T> extends CustomPage<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long boardCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long storeCount;

    public BoardCustomPage(T content, Long requestCursor, Boolean hasNext) {
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

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext
    ) {
        return new BoardCustomPage<>(boardList, requestCursor, hasNext);
    }

    public static BoardCustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext,
        long boardCnt,
        long storeCnt
    ) {
        return new BoardCustomPage<>(boardList, requestCursor, hasNext, boardCnt, storeCnt);
    }

    public void updateBoardLikeStatus(T content) {
        updateContent(content);
    }

}
