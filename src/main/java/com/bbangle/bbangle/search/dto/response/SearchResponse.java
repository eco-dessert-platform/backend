package com.bbangle.bbangle.search.dto.response;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SearchResponse {

    private List<BoardResponseDto> boards;
    private Long itemAllCount;

    public static SearchResponse of(List<BoardResponseDto> boardResponseDtos,
        Long itemAllCount) {
        return SearchResponse.builder()
            .boards(boardResponseDtos)
            .itemAllCount(itemAllCount)
            .build();
    }

    public static SearchResponse empty() {
        List<BoardResponseDto> emptyBoardResponses = Collections.emptyList();

        return SearchResponse.builder()
            .boards(emptyBoardResponses)
            .itemAllCount(0L)
            .build();
    }
}
