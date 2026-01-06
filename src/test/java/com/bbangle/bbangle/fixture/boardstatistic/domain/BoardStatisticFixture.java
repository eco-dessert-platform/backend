package com.bbangle.bbangle.fixture.boardstatistic.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;

public final class BoardStatisticFixture {

    private BoardStatisticFixture() {
    }

    public static BoardStatistic defaultBoardStatisticWithBoard(Board board) {
        return BoardStatistic.builder()
            .boardViewCount(15L)
            .board(board)
            .build();
    }

}
