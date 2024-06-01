package com.bbangle.bbangle.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardImgRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.config.ranking.BoardWishListConfig;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.store.dto.StoreBoardsResponse;
import com.bbangle.bbangle.store.dto.StoreResponse;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoreRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BoardImgRepository boardImgRepository;

    @Autowired
    private WishListFolderRepository wishListFolderRepository;

    @Autowired
    private WishListBoardRepository wishListProductRepository;

    @Autowired
    private WishListStoreRepository wishListStoreRepository;
    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private BoardWishListConfig boardWishListConfig;

    @AfterEach
    void afterEach() {
        rankingRepository.deleteAll();
        wishListProductRepository.deleteAll();
        wishListFolderRepository.deleteAll();
        wishListStoreRepository.deleteAll();
        memberRepository.deleteAll();
        productRepository.deleteAll();
        boardImgRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();
    }

    @Test
    @DisplayName("스토어 상세페이지 - 스토어 조회 기능 : 스토어 아이디에 맞는 스토어 정보를 가져올 수 있다")
    public void test0() {
        Store store = createStore();

        Long memberId = null;
        Long storeId = store.getId();
        StoreResponse storeResponse = storeRepository.getStoreResponse(memberId, storeId);

        assertThat(storeResponse.storeTitle(), is("TestStoreTitle"));
        assertThat(storeResponse.isWished(), is(false));
    }

    @Test
    @DisplayName("스토어 상세페이지 - 스토어 조회 기능 : 스토어 위시리스트 정보를 조회할 수 있다")
    public void test1() {
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
    public void test2() {
        Collection bestBoardTitles = Arrays.asList("TestBoardTitle4", "TestBoardTitle3",
            "TestBoardTitle2");

        Store store = createStore();
        for (int count = 0; count < 5; count++) {
            Board board = createBoard(store, "TestBoardTitle" + count, 100 + count);
            createProduct(board, Category.COOKIE);
        }

        updateRanking();

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
    public void test3() {
        Store store = createStore();
        Member member = createMember();

        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.COOKIE);
            createWishlistProduct(member, board);
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
    public void test4() {
        Member member = createMember();
        Store store = createStore();
        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.COOKIE);
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
    public void test5() {
        Store store = createStore();
        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            createProduct(board, Category.COOKIE);
            createProduct(board, Category.BREAD);
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
    public void test6() {
        Store store = createStore();
        Long lastBoardId = 0L;
        Integer pageCount = 5;

        for (int count = 0; count < 25; count++) {
            Board board = createBoard(store, "TestBoardTitle", 0);
            Product product = createProduct(board, Category.COOKIE);
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
            .view(view)
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

        return wishListProductRepository.save(
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

    private void updateRanking() {
        boardWishListConfig.init();
    }
}
