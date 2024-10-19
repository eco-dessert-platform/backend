package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomBoardService {

    private final BoardRepository boardRepository;

    public BoardCustomPage<List<BoardResponseDto>> getRandomBoardList(
        Long cursorId,
        Long memberId,
        Integer setNumber
    ) {
        List<BoardResponseDao> boardResponseDaoList = boardRepository.getRandomboardList(cursorId, memberId, setNumber);

        return BoardPageGenerator.getBoardPage(boardResponseDaoList, false);
    }

}
