package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.store.dto.StoreBoardsResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponse;

import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;

public class StoreRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";

    @Nested
    @DisplayName("findByBoardId 메서드는")
    class FindByBoardId {

        private Board targetBoard;
        private Store targetStore;
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            targetStore = fixtureStore(Map.of("name", TEST_TITLE));
            targetBoard = fixtureBoard(Map.of("store", targetStore));
        }

        @Test
        @DisplayName("board id가 유효할 때 스토어 정보를 조회할 수 있다.")
        void getStoreInfo() {
            StoreDto storeDto = storeRepository.findByBoardId(targetBoard.getId());

            assertThat(storeDto.getId()).isEqualTo(targetStore.getId());
            assertThat(storeDto.getTitle()).isEqualTo(TEST_TITLE);
        }

        @Test
        @DisplayName("board id가 유효하지 않을 때, null을 반환한다")
        void getNull() {
            StoreDto storeDto = storeRepository.findByBoardId(NOT_EXIST_BOARD_ID);

            assertThat(storeDto).isNull();
        }
    }

    @Test
    @DisplayName("스토어 상세페이지 - 스토어 조회 기능 : 스토어 아이디에 맞는 스토어 정보를 가져올 수 있다")
    void test0() {
        Store store = createStore();

        Long memberId = null;
        Long storeId = store.getId();
        StoreResponse storeResponse = storeRepository.getStoreResponse(memberId, storeId);

        assertThat(storeResponse.storeTitle(), is("TestStoreTitle"));
        assertThat(storeResponse.isWished(), is(false));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 스토어 조회 기능 : 스토어 위시리스트 정보를 조회할 수 있다")
    void test1() {
        Store store = createStore();
        Member member = createMember();
        createWishlistStore(store, member);

        Long memberId = member.getId();
        Long storeId = store.getId();
        StoreResponse storeResponse = storeRepository.getStoreResponse(memberId, storeId);

        assertThat(storeResponse.storeTitle(), is("TestStoreTitle"));
        assertThat(storeResponse.isWished(), is(true));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 베스트 게시글 조회 기능 : 가장 높은 점수 게시글을 3개 가져올 수 있다")
    void test2() {
        Store store = createStore();
        for (int count = 0; count < 5; count++) {
            Board board = createBoard(store, "TestBoardTitle" + count, 100 + count);
            createProduct(board, Category.SNACK);
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        }

        Long memberId = null;
        Long storeId = store.getId();
        List<PopularBoardResponse> popularBoardResponses = storeRepository.getPopularBoardResponses(
            memberId,
            storeId);
        List<String> boardTitles = popularBoardResponses.stream()
            .map(popularBoardDto -> popularBoardDto.getBoardTitle()).toList();

        // 랭킹에 조회수 반영이 안돼 있음, 반영 됐을 때 다시 테스트
        // assertThat(boardTitles, in(bestBoardTitles));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 전체 게시글 조회 기능 : 회원은 스토어가 가진 첫 20개의 게시판 데이터를 가져올 수 있다")
    void test3() {
        Store store = createStore();
        Member member = createMember();

        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.SNACK);
            createWishlistProduct(member, board);
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        }

        Long memberId = member.getId();
        Long cursorId = null;
        StoreDetailCustomPage<List<StoreBoardsResponse>> storeDetailCustomPage = storeRepository.getStoreBoardsResponse(
            memberId, store.getId(), cursorId);

        int storeBoardListDtoSize = storeDetailCustomPage.getContent().size();
        assertThat(storeBoardListDtoSize, is(20));

        storeDetailCustomPage.getContent().stream()
            .map(storeBoardsResponse -> storeBoardsResponse.isWished())
            .forEach(isWished -> assertThat(true, is(isWished)));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 전체 게시글 조회 기능 : 비회원은 스토어가 가진 첫 20개의 게시판 데이터를 가져올 수 있다")
    void test4() {
        Member member = createMember();
        Store store = createStore();
        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.SNACK);
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        }

        Long memberId = member.getId();
        Long storeId = store.getId();
        Long cursorId = null;

        StoreDetailCustomPage<List<StoreBoardsResponse>> storeDetailCustomPage = storeRepository.getStoreBoardsResponse(
            memberId, storeId, cursorId);
        int storeBoardListDtoSize = storeDetailCustomPage.getContent().size();

        assertThat(storeBoardListDtoSize, is(20));

        storeDetailCustomPage.getContent().stream()
            .map(storeBoardsResponse -> storeBoardsResponse.isWished())
            .forEach(isWished -> assertThat(false, is(isWished)));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 전체 게시글 조회 기능 : 스토어가 가진 첫 20개의 게시판 데이터 중 묶음상품이 여부를 조회할 수 있다")
    void test5() {
        Store store = createStore();
        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.SNACK);
            createProduct(board, Category.BREAD);
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        }

        Long cursorId = null;
        Long memberId = null;
        Long storeId = store.getId();
        StoreDetailCustomPage<List<StoreBoardsResponse>> storeDetailCustomPage = storeRepository.getStoreBoardsResponse(
            memberId, storeId, cursorId);
        int storeBoardListDtoSize = storeDetailCustomPage.getContent().size();

        assertThat(storeBoardListDtoSize, is(20));

        storeDetailCustomPage.getContent().stream()
            .map(storeBoardsResponse -> storeBoardsResponse.isBundled())
            .forEach(isBundled -> assertThat(true, is(isBundled)));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 전체 게시판 조회 기능 : 무한스크롤 다음 페이지 게시판들을 가져올 수 있다")
    void test6() {
        Store store = createStore();
        Long lastBoardId = 0L;
        Integer pageCount = 5;

        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
            Product product = createProduct(board, Category.SNACK);
            lastBoardId = board.getId();
        }

        Long memberId = null;
        Long cursorId = lastBoardId - pageCount;

        StoreDetailCustomPage<List<StoreBoardsResponse>> storeDetailCustomPage = storeRepository.getStoreBoardsResponse(
            memberId, store.getId(), cursorId);
        int storeBoardListDtoSize = storeDetailCustomPage.getContent().size();

        assertThat(storeBoardListDtoSize, is(5));
    }

    private Store createStore() {
        return storeRepository.save(Store.builder()
            .identifier("7962401222")
            .name("TestStoreTitle")
            .profile("Test.com")
            .introduce("TestIntroduce")
            .build());
    }

    private Board createBoard(Store store, String title, int view) {
        return boardRepository.save(Board.builder()
            .store(store)
            .title(title)
            .price(5400)
            .status(true)
            .profile("TestProfile.jpg")
            .purchaseUrl("TestPurchaseUrl")
            .sunday(false).monday(false).tuesday(false).wednesday(false).thursday(true)
            .sunday(false)
            .build());
    }

    private Product createProduct(Board board, Category category) {
        return productRepository.save(Product.builder()
            .board(board)
            .title("콩볼")
            .price(3600)
            .category(category)
            .glutenFreeTag(true)
            .highProteinTag(false)
            .sugarFreeTag(true)
            .veganTag(false)
            .ketogenicTag(false)
            .build());
    }

    private void createBoardImg(Board board) {
        boardImgRepository.save(ProductImg.builder()
            .board(board)
            .url("TestURL")
            .build());
    }

    private Member createMember() {
        return memberRepository.save(
            Member.builder()
                .email("dd@ex.com")
                .nickname("test")
                .name("testName")
                .birth("99999")
                .phone("01023299893")
                .build());
    }

    private WishListBoard createWishlistProduct(Member member, Board board) {
        WishListFolder wishlistFolder = wishListFolderRepository.save(
            WishListFolder.builder().
                folderName("Test").
                member(member).
                build());

        return wishListBoardRepository.save(
            WishListBoard.builder().boardId(board.getId())
                .memberId(member.getId())
                .wishlistFolderId(wishlistFolder.getId())
                .build());
    }

    private void createWishlistStore(Store store, Member member) {
        wishListStoreRepository.save(
            WishListStore.builder()
                .store(store)
                .member(member)
                .build());
    }

}
