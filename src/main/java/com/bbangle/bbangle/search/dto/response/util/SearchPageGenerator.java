package com.bbangle.bbangle.search.dto.response.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPageGenerator {

    private static final Long NO_NEXT_CURSOR = -1L;
    private static final Long HAS_NEXT_PAGE_SIZE = BOARD_PAGE_SIZE + 1L;

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
        if (boardResponseDtos.size() == HAS_NEXT_PAGE_SIZE) {
            hasNext = true;
            nextCursor = boardResponseDtos.get(boardResponseDtos.size() - 1).getBoardId();
        }
        boardResponseDtos = boardResponseDtos.stream().limit(BOARD_PAGE_SIZE).toList();

        SearchResponse searchResponse = SearchResponse.of(boardResponseDtos, allItemCount);

        return SearchCustomPage.from(searchResponse, nextCursor, hasNext);
    }

}
