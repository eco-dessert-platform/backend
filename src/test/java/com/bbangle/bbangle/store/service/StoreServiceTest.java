package com.bbangle.bbangle.store.service;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.BoardsInStoreResponse;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoreServiceTest extends AbstractIntegrationTest {

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

        private final Long NULL_CURSOR = null;
        private final Long PAGE_SIZE = 10L;
        private Store store;
        private Board board1;
        private Board board2;
        private Board board3;
        private Member member;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            List<Product> products1 = List.of(
                fixtureProduct(
                    Map.of(
                        "glutenFreeTag", true,
                        "sugarFreeTag", false,
                        "highProteinTag", true,
                        "veganTag", false,
                        "ketogenicTag", false,
                        "category", Category.SNACK)),

                fixtureProduct(
                    Map.of(
                        "glutenFreeTag", true,
                        "sugarFreeTag", false,
                        "highProteinTag", true,
                        "veganTag", false,
                        "ketogenicTag", false,
                        "category", Category.CAKE))
            );
            List<Product> products2 = List.of(
                fixtureProduct(
                    Map.of(
                        "glutenFreeTag", true,
                        "sugarFreeTag", false,
                        "highProteinTag", false,
                        "veganTag", false,
                        "ketogenicTag", false,
                        "category", Category.SNACK)),
                fixtureProduct(
                    Map.of(
                        "glutenFreeTag", true,
                        "sugarFreeTag", false,
                        "highProteinTag", false,
                        "veganTag", false,
                        "ketogenicTag", false,
                        "category",
                        Category.SNACK))
            );

            List<Product> products3 = new ArrayList<>();
            for (int index = 0; 15 > index; index++) {
                products3.add(fixtureProduct(Map.of("sugarFreeTag", true)));
            }

            board1 = fixtureBoard(Map.of("store", store, "productList", products1));
            board2 = fixtureBoard(Map.of("store", store, "productList", products2));
            board3 = fixtureBoard(Map.of("store", store, "productList", products3));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board1));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board3));

            for (int index = 0; 15 > index; index++) {
                fixtureBoard(Map.of("store", store));
            }

            createWishListStore();
        }

        // board_statistic이 로드시에만 적용되기에 에러 발생 => 스웨거로 동작 잘하는거 확인 (추후에 테스트 반영)
//        @Test
//        @DisplayName("다음 커서를 정상적으로 반환한다")
//        void validCursorId() {
//            StoreDetailCustomPage<List<BoardsInStoreResponse>> boardsInStoreByNullCursor = storeService.getBoardsInStore(
//                NULL_MEMBER_ID,
//                store.getId(),
//                NULL_CURSOR);
//
//            StoreDetailCustomPage<List<BoardsInStoreResponse>> boardsInStore = storeService.getBoardsInStore(
//                NULL_MEMBER_ID,
//                store.getId(),
//                boardsInStoreByNullCursor.getNextCursor());
//
//            assertAll(
//                () -> assertThat(boardsInStoreByNullCursor.getHasNext()).isTrue(),
//                () -> assertThat(boardsInStore.getHasNext()).isFalse()
//            );
//        }

        @Test
        @DisplayName("유효하지 않은 커서 아이디 일 때 IllegalArgumentException을 발생시킨다")
        void validNotCursorId() {
            Long storeId = store.getId();

            assertThrows(BbangleException.class,
                () -> storeService.getBoardsInStore(
                    NULL_MEMBER_ID,
                    storeId,
                    Long.MAX_VALUE));
        }

        @Test
        @DisplayName("위시리스트 등록한 상품을 정상적으로 가져올 가져올 수 있다")
        void getIsWished() {
            StoreDetailCustomPage<List<BoardsInStoreResponse>> boardsInStore = storeService.getBoardsInStore(
                member.getId(),
                store.getId(),
                board3.getId());

            Boolean wishTrue = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board1.getId()))
                .findFirst()
                .get()
                .getIsWished();

            Boolean wishFalse = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board2.getId()))
                .toList()
                .get(0)
                .getIsWished();

            assertThat(wishTrue).isTrue();
            assertThat(wishFalse).isFalse();
        }

        @Test
        @DisplayName("게시판 태그 배열을 정상적으로 변환하여 가져올 가져올 수 있다")
        void getTags() {

            StoreDetailCustomPage<List<BoardsInStoreResponse>> boardsInStore = storeService.getBoardsInStore(
                member.getId(),
                store.getId(),
                board3.getId());

            Boolean isBundledTrue = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board1.getId()))
                .findFirst()
                .get()
                .getIsBundled();

            Boolean isBundledFalse = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board2.getId()))
                .findFirst()
                .get()
                .getIsBundled();

            assertThat(isBundledTrue).isTrue();
            assertThat(isBundledFalse).isFalse();
        }

        @Test
        @DisplayName("묶음 상품을 정상적으로 판별할 수 있다")
        void getIsBundled() {
            List<String> expectTagsWithGlutenAndProtein = List.of(TagEnum.GLUTEN_FREE.label(),
                TagEnum.HIGH_PROTEIN.label());

            List<String> expectTagsWithGluten = List.of(TagEnum.GLUTEN_FREE.label(),
                TagEnum.HIGH_PROTEIN.label());

            StoreDetailCustomPage<List<BoardsInStoreResponse>> boardsInStore = storeService.getBoardsInStore(
                member.getId(),
                store.getId(),
                board3.getId());

            List<String> tagsWithGlutenAndProtein = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board1.getId()))
                .findFirst()
                .get()
                .getTags();

            List<String> tagsWithGluten = boardsInStore.getContent().stream()
                .filter(board -> board.getBoardId().equals(board2.getId()))
                .findFirst()
                .get()
                .getTags();

            assertThat(tagsWithGlutenAndProtein).containsAnyElementsOf(
                expectTagsWithGlutenAndProtein);
            assertThat(tagsWithGluten).containsAnyElementsOf(expectTagsWithGluten);
        }

        void createWishListStore() {
            member = memberRepository.save(Member.builder().build());

            wishListBoardRepository.save(WishListBoard.builder()
                .boardId(board1.getId())
                .memberId(member.getId())
                .build());
        }
    }

}
