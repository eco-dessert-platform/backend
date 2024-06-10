package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;

public class BoardStatisticFixture {

    public static BoardStatistic newBoardStatistic(Board board) {
        return BoardStatistic.builder()
            .boardId(board.getId())
            .basicScore(0.0)
            .boardViewCount(0)
            .boardWishCount(0)
            .boardReviewCount(0)
            .boardReviewGrade(0.0)
            .build();
    }

    public static BoardStatistic newBoardStatisticWithBasicScore(Board board, Double basicScore) {
        return BoardStatistic.builder()
            .boardId(board.getId())
            .basicScore(basicScore)
            .boardViewCount(0)
            .boardWishCount(0)
            .boardReviewCount(0)
            .boardReviewGrade(0.0)
            .build();
    }

}
