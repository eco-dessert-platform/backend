package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("getAllBoardTitle 정상 확인")
    void getAllBoardTitle() {
        Board board1 = fixtureBoard(emptyMap());
        Board board2 = fixtureBoard(emptyMap());

        HashMap<Long, String> result = boardRepository.getAllBoardTitle();

        // then
        assertThat(result).containsEntry(board1.getId(), board1.getTitle());
        assertThat(result).containsEntry(board2.getId(), board2.getTitle());
    }

    @Test
    @DisplayName("checkingNullRanking 정상 확인")
    void checkingNullRanking() {
        // given
        Board fixtureBoard = fixtureBoard(emptyMap());
        Board fixtureBoard2 = fixtureBoard(emptyMap());
        BoardStatistic target = fixtureRanking(Map.of("board", fixtureBoard));
        BoardStatistic nonTarget = fixtureRanking(Map.of("board", fixtureBoard2));

        nonTarget.setBoard(null);
        boardStatisticRepository.save(nonTarget);

        // when
        List<Board> result = boardRepository.checkingNullRanking();

        assertThat(result).hasSize(1);
    }
}
