package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RecommendationSimilarBoardFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BoardDetailRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";
    @Autowired
    private BoardStatisticService boardStatisticService;

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

        @Test
        @DisplayName("board id가 유효하지 않을 때, 빈 배열을 반환한다.")
        void getSimilarBoardList() {

            Store store = storeRepository.save(StoreFixture.storeGenerator());

            Board board1 = boardRepository.save(BoardFixture.randomBoard(store));
            Board board2 = boardRepository.save(BoardFixture.randomBoard(store));
            Board board3 = boardRepository.save(BoardFixture.randomBoard(store));
            Board board4 = boardRepository.save(BoardFixture.randomBoard(store));

            productRepository.save(ProductFixture.ketogenicProduct(board1));
            productRepository.save(ProductFixture.ketogenicProduct(board2));
            productRepository.save(ProductFixture.ketogenicProduct(board3));
            productRepository.save(ProductFixture.ketogenicProduct(board4));

            boardStatisticService.updatingNonRankedBoards();

            recommendationSimilarBoardRepository.saveAll(
                RecommendationSimilarBoardFixture.getRandom(
                    board1.getId(),
                    List.of(board2.getId(), board3.getId(), board4.getId())
            ));

            List<SimilarityBoardDto> dtos = boardDetailRepository.findSimilarityBoardByBoardId(
                null,
                board1.getId());

            assertThat(dtos).hasSize(3);
        }
    }
}
