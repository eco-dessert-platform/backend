package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
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
        Store store = StoreFixture.storeGenerator();
        storeRepository.save(store);
        Board fixtureBoard = BoardFixture.randomBoard(store);
        Board fixtureBoard2 = BoardFixture.randomBoard(store);
        List<Board> boards = List.of(fixtureBoard, fixtureBoard2);
        boardRepository.saveAll(boards);
        BoardStatistic nonTarget = BoardStatisticFixture.newBoardStatistic(fixtureBoard2);
        boardStatisticRepository.save(nonTarget);

        // when
        List<Board> result = boardRepository.checkingNullRanking();

        assertThat(result).hasSize(1);
    }
}
