package com.bbangle.bbangle.search.dto.response;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchBoardResponse(
    List<BoardResponseDto> content,
    int itemAllCount,
    int limitItemCount,
    int currentItemCount,
    int pageNumber,
    boolean existNextPage
) {

    public static SearchBoardResponse getEmpty(int pageNumber, int limitItemCount, int itemAllCount){
        return SearchBoardResponse.builder().content(List.of())
                .itemAllCount(itemAllCount)
                .pageNumber(pageNumber)
                .limitItemCount(limitItemCount)
                .currentItemCount(0)
                .existNextPage(false)
                .build();
    }
}
