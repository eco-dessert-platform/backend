package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BoardRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";

    private static final Long NULL_MEMBER_ID = null;

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
                () -> assertThat(boardAndImageDto.boardId()).isNotNull(),
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
        List<TitleDto> boardAllTitleDtos = boardRepository.findAllTitle();

        assertThat(boardAllTitleDtos).hasSize(2);
        boardAllTitleDtos.forEach(boardAllTitleDto -> {
            assertThat(boardAllTitleDto.getBoardId()).isNotNull();
            assertThat(boardAllTitleDto.getTitle()).isNotNull();
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

    @Nested
    @DisplayName("getTopBoardInfo 메서드는")
    class GetTopBoardInfo {

        private Store store;
        private Member member;
        private Board firstBoard;
        private Board secondBoard;
        private Board thirdBoard;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            firstBoard = boardRepository.save(BoardFixture.randomBoard(store));
            secondBoard = boardRepository.save(BoardFixture.randomBoard(store));
            thirdBoard = boardRepository.save(BoardFixture.randomBoard(store));

            productRepository.save(ProductFixture.randomProduct(firstBoard));
            productRepository.save(ProductFixture.randomProduct(secondBoard));
            productRepository.save(ProductFixture.randomProduct(thirdBoard));

            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(firstBoard, 103d));
            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(secondBoard, 102d));
            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(thirdBoard, 101d));

            createWishListStore();
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            updateBoardStatistic.updateStatistic();
            List<BoardInfoDto> rankBoardIds = boardRepository.findBestBoards(NULL_MEMBER_ID,
                store.getId());

            assertThat(rankBoardIds).hasSize(3);
            assertThat(rankBoardIds.get(0).getBoardId()).isEqualTo(firstBoard.getId());
            assertThat(rankBoardIds.get(1).getBoardId()).isEqualTo(secondBoard.getId());
            assertThat(rankBoardIds.get(2).getBoardId()).isEqualTo(thirdBoard.getId());
        }

        void createWishListStore() {
            member = memberRepository.save(Member.builder().build());
            wishListBoardRepository.save(WishListBoard.builder()
                .boardId(firstBoard.getId())
                .memberId(member.getId())
                .build());
        }
    }

    @Nested
    @DisplayName("getBoardIds 메서드는")
    class GetBoardIds {

        private static final Long NULL_CURSOR = null;
        private Store store;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            for (int index = 0; 20 > index; index++) {
                fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
            }
        }
    }

    @Nested
    @DisplayName("findByBoardIds 메서드는")
    class FindByBoardIds {

        private Store store;
        private Board board1;
        private Board board2;
        private Board board3;
        private Board board4;
        private Board board5;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            Product glutenFreeTagProduct = fixtureProduct(Map.of(
                "glutenFreeTag", true,
                "highProteinTag", false,
                "sugarFreeTag", false,
                "veganTag", false,
                "ketogenicTag", false,
                "orderStartDate", LocalDateTime.now(),
                "soldout", true
            ));

            Map<String, Object> params = new HashMap<>();
            params.put("glutenFreeTag", false);
            params.put("highProteinTag", true);
            params.put("sugarFreeTag", false);
            params.put("veganTag", false);
            params.put("ketogenicTag", false);
            params.put("orderStartDate", null);
            params.put("soldout", false);

            Product highProteinTagProduct = fixtureProduct(params);

            Product sugarFreeTagProduct = fixtureProduct(Map.of(
                "glutenFreeTag", false,
                "highProteinTag", false,
                "sugarFreeTag", true,
                "veganTag", false,
                "ketogenicTag", false
            ));
            Product veganTagProduct = fixtureProduct(Map.of(
                "glutenFreeTag", false,
                "highProteinTag", false,
                "sugarFreeTag", false,
                "veganTag", true,
                "ketogenicTag", false,
                "category", Category.COOKIE
            ));

            Product veganTagProduct2 = fixtureProduct(Map.of(
                "glutenFreeTag", false,
                "highProteinTag", false,
                "sugarFreeTag", false,
                "veganTag", true,
                "ketogenicTag", false,
                "category", Category.SNACK
            ));

            Product ketogenicTagProduct = fixtureProduct(Map.of(
                "glutenFreeTag", false,
                "highProteinTag", false,
                "sugarFreeTag", false,
                "veganTag", false,
                "ketogenicTag", true,
                "category", Category.SNACK
            ));

            Product ketogenicTagProduct2 = fixtureProduct(Map.of(
                "glutenFreeTag", false,
                "highProteinTag", false,
                "sugarFreeTag", false,
                "veganTag", false,
                "ketogenicTag", true,
                "category", Category.SNACK
            ));

            board1 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "productList", List.of(glutenFreeTagProduct)
            ));
            board2 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "productList", List.of(highProteinTagProduct)
            ));
            board3 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "productList", List.of(sugarFreeTagProduct)
            ));
            board4 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "productList", List.of(veganTagProduct, veganTagProduct2)
            ));

            board5 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "productList", List.of(ketogenicTagProduct, ketogenicTagProduct2)
            ));

            boardStatisticRepository.saveAll(
                List.of(BoardStatisticFixture.newBoardStatistic(board1),
                    BoardStatisticFixture.newBoardStatistic(board2),
                    BoardStatisticFixture.newBoardStatistic(board3),
                    BoardStatisticFixture.newBoardStatistic(board4),
                    BoardStatisticFixture.newBoardStatistic(board5))
            );
        }
    }
}
