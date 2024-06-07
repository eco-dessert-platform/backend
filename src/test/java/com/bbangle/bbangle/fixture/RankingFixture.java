package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;

public class RankingFixture {

    public static BoardStatistic newRanking(Board board) {
        return BoardStatistic.builder()
            .board(board)
            .recommendScore(0.0)
            .popularScore(0.0)
            .build();
    }

}
