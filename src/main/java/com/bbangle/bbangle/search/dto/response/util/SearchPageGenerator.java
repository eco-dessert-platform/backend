package com.bbangle.bbangle.search.dto.response.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.page.ProcessedDataCursorResponse;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPageGenerator {

    public static ProcessedDataCursorResponse<BoardResponseDto, SearchResponse> getBoardPage(
        List<BoardResponseDao> boardDaos,
        Boolean isInFolder,
        Long allItemCount
    ) {
        if (boardDaos.isEmpty()) {
            return ProcessedDataCursorResponse.empty(SearchResponse.empty());
        }

        List<BoardResponseDto> boardResponseDtos = BoardPageGenerator.getBoardPage(boardDaos,
            isInFolder).getContent();

        return ProcessedDataCursorResponse.of(
            boardResponseDtos,
            BOARD_PAGE_SIZE,
            BoardResponseDto::getBoardId,
            boardResponseDtos1 -> SearchResponse.of(boardResponseDtos1, allItemCount));
    }
}
