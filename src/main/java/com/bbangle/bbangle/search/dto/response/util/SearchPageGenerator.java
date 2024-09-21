package com.bbangle.bbangle.search.dto.response.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPageGenerator {

    private static final Long NO_NEXT_CURSOR = -1L;

    public static SearchCustomPage<SearchResponse> getBoardPage(
        List<BoardResponseDao> boardDaos,
        Boolean isInFolder,
        Long allItemCount
    ) {
        if (boardDaos.isEmpty()) {
            return SearchCustomPage.emptyPage();
        }

        List<BoardResponseDto> boardResponseDtos = BoardPageGenerator.getBoardPage(boardDaos, isInFolder).getContent();

        Long nextCursor = NO_NEXT_CURSOR;
        boolean hasNext = false;
        if (boardDaos.size() > BOARD_PAGE_SIZE) {
            hasNext = true;
            nextCursor = boardResponseDtos.get(boardResponseDtos.size() - 1).getBoardId() - 1;
            boardDaos.remove(boardDaos.size() - 1);
        }
        boardResponseDtos = boardResponseDtos.stream().limit(BOARD_PAGE_SIZE).toList();

        SearchResponse searchResponse = SearchResponse.of(boardResponseDtos, allItemCount);

        return SearchCustomPage.from(searchResponse, nextCursor, hasNext);
    }

}
