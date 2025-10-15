package com.bbangle.bbangle.board.customer.service;

import com.bbangle.bbangle.board.repository.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.customer.dto.BoardResponse;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomBoardService {

    private final BoardRepository boardRepository;
    private final BoardService boardService;

    public CursorPageResponse<BoardResponse> getRandomBoardList(
            Long cursorId,
            Long memberId,
            Integer setNumber
    ) {
        List<BoardThumbnailDao> boardThumbnailDaoList = boardRepository.getRandomboardList(cursorId, memberId, setNumber);

        return boardService.getResponseFromDao(boardThumbnailDaoList);
    }
}
