package com.bbangle.bbangle.board.repository.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardResponses;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.time.LocalDateTime;
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

/**
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPageGenerator {

    private static final Long NO_NEXT_CURSOR = -1L;
    private static final Long HAS_NEXT_PAGE_SIZE = BOARD_PAGE_SIZE + 1L;

    /**
     *
     * 책임:
     *          1. list가 비었다면 빈 페이지를 반환함     -> 공통 CustomPage에서 커버가능
     *          2. Dao -> DTO 변환
     *          3. 커서 조절 및 DTO리스트 하나 삭제       -> 공통 CustomPage에서 커버가능
     *
     * 거슬리는 점:
     *              1. 플래그를 전달받음
     *              2. 함수가 20줄을 넘는다(아주 사소)
     *              3. 나머지는 쿼리로 한 번에 받지 않았기에 함수가 복잡하다(중요)
     */
    public static BoardCustomPage<List<BoardResponse>> getBoardPage(
        List<BoardResponseDao> boardDaos, Boolean isInFolder
    ) {
        if (boardDaos.isEmpty()) {
            return BoardCustomPage.emptyPage();
        }

        BoardResponses boardResponses = BoardResponses.convertToBoardResponse(boardDaos, isInFolder);

        Long nextCursor = NO_NEXT_CURSOR;
        boolean hasNext = false;
        if (boardResponses.size() == HAS_NEXT_PAGE_SIZE) {
            hasNext = true;
            nextCursor = boardResponses.get(boardResponses.size() - 1)
                .getBoardId();
        }
        boardResponses = boardResponses.stream()
            .limit(BOARD_PAGE_SIZE)
            .toList();

        return BoardCustomPage.from(boardResponses, nextCursor, hasNext);
    }
}
