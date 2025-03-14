package com.bbangle.bbangle.board.repository.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardResponses;
import com.bbangle.bbangle.page.CursorPageResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPageGenerator {

    public static CursorPageResponse<BoardResponse> getBoardPage(
            List<BoardResponseDao> boardDaos, Boolean isInFolder
    ) {
        BoardResponses boardResponses = BoardResponses.convertToBoardResponse(boardDaos, isInFolder);
        return CursorPageResponse.of(boardResponses.boardResponses(), BOARD_PAGE_SIZE, BoardResponse::getBoardId);
    }
}
