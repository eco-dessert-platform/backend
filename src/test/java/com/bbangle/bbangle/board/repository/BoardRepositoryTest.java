package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BoardRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";

    @Nested
    @DisplayName("findBoardAndBoardImageByBoardId 메서드는")
    class FindBoardAndBoardImageByBoardId {

        private Board targetBoard;
        private String TEST_URL = "www.TESTURL.com";
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of("title", TEST_TITLE));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
        }

        @Test
        @DisplayName("board id가 유효할 때 게시판 정보, 게시판 이미지를 조회할 수 있다.")
        void getBoardInfoAndImages() {
            List<BoardAndImageDto> boardAndImageDtos = boardRepository
                .findBoardAndBoardImageByBoardId(targetBoard.getId());

            assertThat(boardAndImageDtos).hasSize(2);

            BoardAndImageDto boardAndImageDto = boardAndImageDtos.stream()
                .findFirst()
                .get();

            assertAll(
                "BoardAndImageDto는 null이 없어야한다.",
                () -> assertThat(boardAndImageDto.id()).isNotNull(),
                () -> assertThat(boardAndImageDto.url()).isNotNull()
            );
        }

        @Test
        @DisplayName("board id가 유효하지 않을 때, 빈 배열을 반환한다.")
        void getEmptyList() {
            List<BoardAndImageDto> boardAndImageDtos = boardRepository
                .findBoardAndBoardImageByBoardId(NOT_EXIST_BOARD_ID);

            assertThat(boardAndImageDtos).isEmpty();
        }
    }

    @Test
    @DisplayName("게시판 전체의 아이디와 게시글명을 가져올 수 있다.")
    void getAllBoardTitle() {
        fixtureBoard(Map.of("title", TEST_TITLE));
        fixtureBoard(Map.of("title", TEST_TITLE));
        List<BoardAllTitleDto> boardAllTitleDtos = boardRepository.findTitleByBoardAll();

        assertThat(boardAllTitleDtos).hasSize(2);
        boardAllTitleDtos.forEach(boardAllTitleDto -> {
            assertThat(boardAllTitleDto.boardId()).isNotNull();
            assertThat(boardAllTitleDto.Title()).isNotNull();
        });

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
