package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.ranking.domain.Ranking;
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
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of("title", TEST_TITLE));
            fixtureBoardImage(Map.of("board", targetBoard));
            fixtureBoardImage(Map.of("board", targetBoard));
        }

        @Test
        @DisplayName("board id가 유효할 때 게시판 정보, 게시판 이미지를 조회할 수 있다.")
        void getBoardInfoAndImages() {
            List<BoardAndImageDto> boardAndImageDtos = boardRepository
                .findBoardAndBoardImageByBoardId(targetBoard.getId());

            assertThat(boardAndImageDtos).hasSize(2);

            BoardAndImageDto boardAndImageDto = boardAndImageDtos.stream().findFirst().get();

            assertAll(
                "BoardAndImageDto는 null이 없어야한다.",
                () -> assertThat(boardAndImageDto.id()).isNotNull(),
                () -> assertThat(boardAndImageDto.profile()).isNotNull(),
                () -> assertThat(boardAndImageDto.title()).isNotNull(),
                () -> assertThat(boardAndImageDto.price()).isNotNull(),
                () -> assertThat(boardAndImageDto.purchaseUrl()).isNotNull(),
                () -> assertThat(boardAndImageDto.deliveryFee()).isNotNull()
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

    @Nested
    @DisplayName("getProductDto 메서드는")
    class GetProductDto {

        private Board targetBoard;
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            List<Product> products = List.of(
                fixtureProduct(Map.of()),
                fixtureProduct(Map.of()),
                fixtureProduct(Map.of()));

            targetBoard = fixtureBoard(Map.of("productList", products));
        }

        @Test
        @DisplayName("board id가 유효할 때 상품 정보들을 조회할 수 있다.")
        void getProductInfo() {
            List<ProductDto> productDtos = boardRepository.getProductDto(targetBoard.getId());
            assertThat(productDtos).hasSize(3);

            ProductDto productDto = productDtos.stream().findFirst().get();
            assertAll(
                "ProductDto는 null이 없어야한다.",
                () -> assertThat(productDto.productId()).isNotNull(),
                () -> assertThat(productDto.productTitle()).isNotNull(),
                () -> assertThat(productDto.category()).isNotNull(),
                () -> assertThat(productDto.glutenFreeTag()).isNotNull(),
                () -> assertThat(productDto.highProteinTag()).isNotNull(),
                () -> assertThat(productDto.sugarFreeTag()).isNotNull(),
                () -> assertThat(productDto.veganTag()).isNotNull(),
                () -> assertThat(productDto.ketogenicTag()).isNotNull()
            );
        }

        @Test
        @DisplayName("board id가 유효하지 않을 때, 빈 배열을 반환한다.")
        void getEmptyList() {
            List<ProductDto> productDtos = boardRepository.getProductDto(NOT_EXIST_BOARD_ID);

            assertThat(productDtos).isEmpty();
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
        Board fixtureBoard = fixtureBoard(emptyMap());
        Board fixtureBoard2 = fixtureBoard(emptyMap());
        Ranking target = fixtureRanking(Map.of("board", fixtureBoard));
        Ranking nonTarget = fixtureRanking(Map.of("board", fixtureBoard2));

        nonTarget.setBoard(null);
        rankingRepository.save(nonTarget);

        // when
        List<Board> result = boardRepository.checkingNullRanking();

        assertThat(result).hasSize(1);
    }
}