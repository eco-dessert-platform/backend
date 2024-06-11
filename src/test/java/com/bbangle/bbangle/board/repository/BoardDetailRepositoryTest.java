package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class BoardDetailRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";

    @Nested
    @DisplayName("findByBoardId 메서드는")
    class FindByBoardId {

        private Board targetBoard;
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of("title", TEST_TITLE));
            fixtureBoardDetail(Map.of("board", targetBoard));
            fixtureBoardDetail(Map.of("board", targetBoard));
        }

        @Test
        @DisplayName("board id가 유효할 때, 상세 이미지를 조회할 수 있다")
        void getBoardDetailTest() {
            List<String> boardDetailDtos = boardDetailRepository
                .findByBoardId(targetBoard.getId());

            assertThat(boardDetailDtos).hasSize(2);

            assertThat(boardDetailDtos).isNotEmpty();
        }

        @Test
        @DisplayName("board id가 유효하지 않을 때, 빈 배열을 반환한다.")
        void getEmptyList() {
            List<String> boardDetailDtos = boardDetailRepository
                .findByBoardId(NOT_EXIST_BOARD_ID);

            assertThat(boardDetailDtos).isEmpty();
        }
    }

}
