package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import java.math.BigDecimal;

public class BoardStatisticFixture {

    public static BoardStatistic newBoardStatistic(Board board) {
        return BoardStatistic.builder()
            .boardId(board.getId())
            .basicScore(0.0)
            .boardViewCount(0L)
            .boardWishCount(0L)
            .boardReviewCount(0L)
            .boardReviewGrade(BigDecimal.ZERO)
            .build();
    }

    public static BoardStatistic newBoardStatisticWithBasicScore(Board board, Double basicScore) {
        return BoardStatistic.builder()
            .boardId(board.getId())
            .basicScore(basicScore)
            .boardViewCount(0L)
            .boardWishCount(0L)
            .boardReviewCount(0L)
            .boardReviewGrade(BigDecimal.ZERO)
            .build();
    }

}
