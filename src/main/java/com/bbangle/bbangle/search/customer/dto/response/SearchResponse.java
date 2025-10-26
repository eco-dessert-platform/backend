package com.bbangle.bbangle.search.customer.dto.response;

import com.bbangle.bbangle.search.customer.dto.SearchBoardResponseDto;
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

    private List<SearchBoardResponseDto> boards;
    private Long itemAllCount;

    public static SearchResponse of(List<SearchBoardResponseDto> boardResponseDtos,
        Long itemAllCount) {
        return SearchResponse.builder()
            .boards(boardResponseDtos)
            .itemAllCount(itemAllCount)
            .build();
    }

    public static SearchResponse empty() {
        List<SearchBoardResponseDto> emptyBoardResponses = Collections.emptyList();

        return SearchResponse.builder()
            .boards(emptyBoardResponses)
            .itemAllCount(0L)
            .build();
    }
}
