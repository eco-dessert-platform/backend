package com.bbangle.bbangle.page;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public final class CustomPage<T> {

    private final T content;
    private final Long requestCursor;
    private final Boolean hasNext;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long boardCnt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long storeCnt;

    public static CustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext
    ) {
        return new CustomPage<>(boardList, requestCursor, hasNext);
    }

    public static CustomPage<List<BoardResponseDto>> from(
        List<BoardResponseDto> boardList,
        Long requestCursor,
        boolean hasNext,
        long boardCnt,
        long storeCnt
    ) {
        return new CustomPage<>(boardList, requestCursor, hasNext, boardCnt, storeCnt);
    }

}
