package com.bbangle.bbangle.ranking.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.ranking.repository.BoardStatisticRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardStatisticService {

    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardRepository boardRepository;

    public void updatingNonRankedBoards() {
        List<Board> unRankedBoards = boardRepository.checkingNullRanking();
        List<BoardStatistic> boardStatistics = new ArrayList<>();
        unRankedBoards.stream()
            .map(board -> BoardStatistic.builder()
                .boardId(board.getId())
                .basicScore(0.0)
                .boardReviewCount(0)
                .boardWishCount(0)
                .boardViewCount(0)
                .boardReviewGrade(0.0)
                .build())
            .forEach(boardStatistics::add);
        boardStatisticRepository.saveAll(boardStatistics);
    }

}