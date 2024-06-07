package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;

public class RankingFixture {

    public static BoardStatistic newRanking(Board board) {
        return BoardStatistic.builder()
            .boardId(board.getId())
            .basicScore(0.0)
            .boardViewCount(0)
            .boardWishCount(0)
            .boardReviewCount(0)
            .boardReviewGrade(0.0)
            .build();
    }

}
