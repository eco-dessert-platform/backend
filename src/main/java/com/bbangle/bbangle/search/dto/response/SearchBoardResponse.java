package com.bbangle.bbangle.search.dto.response;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchBoardResponse(
    List<BoardResponseDto> content,
    Long itemAllCount,
    int limitItemCount,
    int currentItemCount,
    int pageNumber,
    boolean existNextPage
) {

    public static SearchBoardResponse getEmpty(int pageNumber, int limitItemCount, Long itemAllCount){
        return SearchBoardResponse.builder().content(Collections.emptyList())
                .itemAllCount(itemAllCount)
                .pageNumber(pageNumber)
                .limitItemCount(limitItemCount)
                .currentItemCount(0)
                .existNextPage(false)
                .build();
    }
}
