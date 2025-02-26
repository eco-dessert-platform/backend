package com.bbangle.bbangle.store.service;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.page.CursorPageResponse;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StoreServiceTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";
    private static final Long NULL_CURSOR = null;
    private static final Long NULL_MEMBER_ID = null;

    @Nested
    @DisplayName("store 조회 서비스 로직 테스트")
    class GetStoreList {

        @BeforeEach
        void saveStoreList() {
            for (int i = 0; i < 30; i++) {
                Store store = StoreFixture.storeGenerator();
                storeRepository.save(store);
            }
        }
    }

    @Nested
    @DisplayName("getBoardsInStore 메서드는")
    class GetBoardsInStore {

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
                "products", List.of(glutenFreeTagProduct)
            ));
            board2 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "products", List.of(highProteinTagProduct)
            ));
            board3 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "products", List.of(sugarFreeTagProduct)
            ));
            board4 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "products", List.of(veganTagProduct, veganTagProduct2)
            ));

            board5 = fixtureBoard(Map.of(
                "store", store, "title", TEST_TITLE,
                "products", List.of(ketogenicTagProduct, ketogenicTagProduct2)
            ));

            boardStatisticRepository.saveAll(
                List.of(BoardStatisticFixture.newBoardStatistic(board1),
                    BoardStatisticFixture.newBoardStatistic(board2),
                    BoardStatisticFixture.newBoardStatistic(board3),
                    BoardStatisticFixture.newBoardStatistic(board4),
                    BoardStatisticFixture.newBoardStatistic(board5))
            );
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getTags() {

            CursorPageResponse<BoardInfoDto> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            List<String> board1Tag = boardsInStoreDtos.getData().get(4).getTags();
            assertAll(
                () -> assertThat(board1Tag).contains(TagEnum.GLUTEN_FREE.label()),
                () -> assertThat(board1Tag).doesNotContain(
                    TagEnum.SUGAR_FREE.label(),
                    TagEnum.VEGAN.label(),
                    TagEnum.HIGH_PROTEIN.label(),
                    TagEnum.KETOGENIC.label()
                ));

            List<String> board2Tag = boardsInStoreDtos.getData().get(3).getTags();
            assertAll(
                () -> assertThat(board2Tag)
                    .contains(TagEnum.HIGH_PROTEIN.label())
                    .doesNotContain(
                        TagEnum.GLUTEN_FREE.label(),
                        TagEnum.SUGAR_FREE.label(),
                        TagEnum.VEGAN.label(),
                        TagEnum.KETOGENIC.label()
                    )
            );

            List<String> board3Tag = boardsInStoreDtos.getData().get(2).getTags();
            assertAll(
                () -> assertThat(board3Tag)
                    .contains(TagEnum.SUGAR_FREE.label())
                    .doesNotContain(
                        TagEnum.GLUTEN_FREE.label(),
                        TagEnum.HIGH_PROTEIN.label(),
                        TagEnum.VEGAN.label(),
                        TagEnum.KETOGENIC.label()
                    )
            );

            List<String> board4Tag = boardsInStoreDtos.getData().get(1).getTags();
            assertAll(
                () -> assertThat(board4Tag)
                    .contains(TagEnum.VEGAN.label())
                    .doesNotContain(
                        TagEnum.GLUTEN_FREE.label(),
                        TagEnum.HIGH_PROTEIN.label(),
                        TagEnum.SUGAR_FREE.label(),
                        TagEnum.KETOGENIC.label()
                    )
            );

            List<String> board5Tag = boardsInStoreDtos.getData().get(0).getTags();
            assertAll(
                () -> assertThat(board5Tag)
                    .contains(TagEnum.KETOGENIC.label())
                    .doesNotContain(
                        TagEnum.GLUTEN_FREE.label(),
                        TagEnum.HIGH_PROTEIN.label(),
                        TagEnum.SUGAR_FREE.label(),
                        TagEnum.VEGAN.label()
                    )
            );
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getBbangKetting() {
            CursorPageResponse<BoardInfoDto> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(
                boardsInStoreDtos.getData().get(4).getIsBbangcketing()).isTrue();
            AssertionsForClassTypes.assertThat(
                boardsInStoreDtos.getData().get(3).getIsBbangcketing()).isFalse();
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getIsSoldOut() {
            CursorPageResponse<BoardInfoDto> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getData().get(4).getIsSoldOut())
                .isTrue();
            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getData().get(3).getIsSoldOut())
                .isFalse();
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getIsBundled() {
            CursorPageResponse<BoardInfoDto> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getData().get(1).getIsBundled())
                .isTrue();
            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getData().get(0).getIsBundled())
                .isFalse();
        }

        @Test
        @DisplayName("스토어에 상품이 없을 때 빈 데이터를 반환한다")
        void getEmptyOnStoreHavingNot() {
            //given
            Store emptyStore = fixtureStore(emptyMap());

            //when
            CursorPageResponse boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, emptyStore.getId(), NULL_CURSOR);  // board id desc 임

            //then
            assertThat(boardsInStoreDtos.getData()).isEmpty();
        }
    }
}
