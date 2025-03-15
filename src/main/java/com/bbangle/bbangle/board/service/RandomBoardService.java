package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.page.CursorPageResponse;
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
        List<BoardResponseDao> boardResponseDaoList = boardRepository.getRandomboardList(cursorId, memberId, setNumber);

        return boardService.getResponseFromDao(boardResponseDaoList, memberId, false);
    }
}
