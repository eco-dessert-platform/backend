package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardStatisticService {

    private static final Double BOARD_WISH_SCORE = 50.0;
    private static final Double BOARD_VIEW_SCORE = 1.0;
    private static final Double BOARD_REVIEW_SCORE = 50.0;

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
                .boardReviewGrade(BigDecimal.ZERO)
                .build())
            .forEach(boardStatistics::add);
        boardStatisticRepository.saveAll(boardStatistics);
    }

    @Async
    @Transactional
    public void updateViewCount(Long boardId) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateViewCount();
        boardStatistic.updateBasicScore(BOARD_VIEW_SCORE, true);
    }

    @Async
    @Transactional
    public void updateWishCount(Long boardId, boolean isWish) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));

        boardStatistic.updateWishCount(isWish);
        boardStatistic.updateBasicScore(BOARD_WISH_SCORE, isWish);
    }

    @Async
    @Transactional
    public void updateReviewCount(
        Long boardId,
        BigDecimal rate,
        boolean isCreate
    ) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateReviewGrade(rate, isCreate);
        boardStatistic.updateReviewCount(isCreate);
        boardStatistic.updateBasicScore(BOARD_REVIEW_SCORE, isCreate);
    }

}
