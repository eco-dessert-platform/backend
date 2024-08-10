package com.bbangle.bbangle.store.service;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
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
    private Long memberId;

    @BeforeEach
    void setup() {
        wishListStoreRepository.deleteAll();
        wishListFolderRepository.deleteAll();
        storeRepository.deleteAll();
        memberRepository.deleteAll();

        Member member = MemberFixture.createKakaoMember();
        memberId = memberService.getFirstJoinedMember(member);
    }

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

        @Test
        @DisplayName("정상적으로 첫 페이지를 조회한다")
        void getFirstPage() {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> list = storeService.getList(
                NULL_CURSOR,
                NULL_MEMBER_ID
            );
            List<StoreResponseDto> content = list.getContent();
            Boolean hasNext = list.getHasNext();

            //then
            assertThat(content).hasSize(20);
            assertThat(hasNext).isTrue();
        }

        @Test
        @DisplayName("정상적으로 마지막 페이지를 조회한다")
        void getLastPage() {
            //given
            StoreCustomPage<List<StoreResponseDto>> firstPage = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            Long nextCursor = firstPage.getNextCursor();

            //when
            StoreCustomPage<List<StoreResponseDto>> lastPage = storeService.getList(nextCursor,
                NULL_MEMBER_ID);

            List<StoreResponseDto> lastPageContent = lastPage.getContent();
            Boolean lastPageHasNext = lastPage.getHasNext();
            Long lastPageNextCursor = lastPage.getNextCursor();

            //then
            assertThat(lastPageContent).hasSize(10);
            assertThat(lastPageHasNext).isFalse();
        }

        @Test
        @DisplayName("마지막 자료를 조회하는 경우 nextCursor는 -1을 가리킨다")
        void getLastContent() {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> firstPage = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            Long nextCursor = firstPage.getNextCursor();
            StoreCustomPage<List<StoreResponseDto>> lastPage = storeService.getList(nextCursor,
                NULL_MEMBER_ID);
            Long lastContentCursor = lastPage.getNextCursor();

            StoreCustomPage<List<StoreResponseDto>> noContent = storeService.getList(
                lastContentCursor,
                NULL_MEMBER_ID);

            //then
            assertThat(noContent.getContent()).isEmpty();
            assertThat(noContent.getNextCursor()).isEqualTo(-1L);
        }

        @Test
        @DisplayName("좋아요를 누른 store는 isWished가 true로 반환된다")
        void getWishedContent() throws Exception {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> before = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            StoreResponseDto first = before.getContent()
                .stream()
                .findFirst()
                .orElseThrow(Exception::new);
            wishListStoreService.save(memberId, first.getStoreId());

            //then
            StoreResponseDto wishedContent = storeService.getList(NULL_CURSOR, memberId)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(Exception::new);
            assertThat(wishedContent.getIsWished()).isTrue();
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
                "soldout",true
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

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getTags() {

            StoreDetailCustomPage<List<BoardInfoDto>> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            TagsDao board1Tag = boardsInStoreDtos.getContent().get(4).getTags();
            assertAll(
                () -> AssertionsForClassTypes.assertThat(board1Tag.glutenFreeTag()).isTrue(),
                () -> AssertionsForClassTypes.assertThat(board1Tag.highProteinTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board1Tag.sugarFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board1Tag.veganTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board1Tag.ketogenicTag()).isFalse());

            TagsDao board2Tag = boardsInStoreDtos.getContent().get(3).getTags();
            assertAll(
                () -> AssertionsForClassTypes.assertThat(board2Tag.glutenFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board2Tag.highProteinTag()).isTrue(),
                () -> AssertionsForClassTypes.assertThat(board2Tag.sugarFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board2Tag.veganTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board2Tag.ketogenicTag()).isFalse());

            TagsDao board3Tag = boardsInStoreDtos.getContent().get(2).getTags();
            assertAll(
                () -> AssertionsForClassTypes.assertThat(board3Tag.glutenFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board3Tag.highProteinTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board3Tag.sugarFreeTag()).isTrue(),
                () -> AssertionsForClassTypes.assertThat(board3Tag.veganTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board3Tag.ketogenicTag()).isFalse());

            TagsDao board4Tag = boardsInStoreDtos.getContent().get(1).getTags();
            assertAll(
                () -> AssertionsForClassTypes.assertThat(board4Tag.glutenFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board4Tag.highProteinTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board4Tag.sugarFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board4Tag.veganTag()).isTrue(),
                () -> AssertionsForClassTypes.assertThat(board4Tag.ketogenicTag()).isFalse());

            TagsDao board5Tag = boardsInStoreDtos.getContent().get(0).getTags();
            assertAll(
                () -> AssertionsForClassTypes.assertThat(board5Tag.glutenFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board5Tag.highProteinTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board5Tag.sugarFreeTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board5Tag.veganTag()).isFalse(),
                () -> AssertionsForClassTypes.assertThat(board5Tag.ketogenicTag()).isTrue());
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getBbangKetting() {
            StoreDetailCustomPage<List<BoardInfoDto>> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(4).getIsNotification()).isTrue();
            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(3).getIsNotification()).isFalse();
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getIsSoldOut() {
            StoreDetailCustomPage<List<BoardInfoDto>> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(4).getIsSoldOut()).isTrue();
            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(3).getIsSoldOut()).isFalse();
        }

        @Test
        @DisplayName("태그 정보를 성공적으로 가져올 수 있다")
        void getIsBundled() {
            StoreDetailCustomPage<List<BoardInfoDto>> boardsInStoreDtos = storeService.getBoardsInStore(
                NULL_MEMBER_ID, store.getId(), NULL_CURSOR);  // board id desc 임

            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(1).getIsBundled()).isTrue();
            AssertionsForClassTypes.assertThat(boardsInStoreDtos.getContent().get(0).getIsBundled()).isFalse();
        }

    }


}
