package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.constant.FolderBoardSortType;
import com.bbangle.bbangle.board.constant.SortType;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.ranking.UpdateBoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

class BoardServiceTest extends AbstractIntegrationTest {

    private static final Long NULL_CURSOR = null;
    private static final SortType DEFAULT_SORT_TYPE = SortType.RECOMMEND;
    private static final Long NULL_MEMBER = null;
    private final String TEST_TITLE = "TestTitle";

    @Autowired
    UpdateBoardStatistic updateBoardStatistic;

    Board board;
    Board board2;
    Store store;
    Store store2;

    @BeforeEach
    void setup() {
        store = StoreFixture.storeGenerator();
        storeRepository.save(store);
        store2 = StoreFixture.storeGenerator();
        storeRepository.save(store2);

        board = BoardFixture.randomBoardWithMoney(store, 1000);
        board2 = BoardFixture.randomBoardWithMoney(store, 10000);

        board = boardRepository.save(board);
        board2 = boardRepository.save(board2);

        boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatistic(board)
        );
        boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatistic(board2)
        );
    }


    @Test
    @DisplayName("필터가 없는 경우에도 모든 리스트를 정상적으로 조회한다.")
    void showAllList() {
        //given, when
        Product product1 = ProductFixture.productWithFullInfo(board,
                true,
                true,
                true,
                true,
                true,
                Category.BREAD);

        Product product2 = ProductFixture.productWithFullInfo(board2,
                false,
                true,
                true,
                true,
                false,
                Category.BREAD);

        Product product3 = ProductFixture.productWithFullInfo(board2,
                true,
                false,
                true,
                false,
                false,
                Category.BREAD);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
                .build();

        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        BoardResponse response1 = boardList.getContent()
                .get(0);
        BoardResponse response2 = boardList.getContent()
                .get(1);

        //then
        assertThat(boardList.getContent()).hasSize(2);

        assertThat(response2.getTags()
                .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(true);
        assertThat(response2.getTags()
                .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(true);
        assertThat(response2.getTags()
                .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response2.getTags()
                .contains(TagEnum.VEGAN.label())).isEqualTo(true);
        assertThat(response2.getTags()
                .contains(TagEnum.KETOGENIC.label())).isEqualTo(true);
        assertThat(response1.getTags()
                .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(true);
        assertThat(response1.getTags()
                .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(true);
        assertThat(response1.getTags()
                .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response1.getTags()
                .contains(TagEnum.VEGAN.label())).isEqualTo(true);
        assertThat(response1.getTags()
                .contains(TagEnum.KETOGENIC.label())).isEqualTo(false);
    }

    @Test
    @DisplayName("glutenFree 제품이 포함된 게시물만 조회한다.")
    void showListFilterByGlutenFree() {
        //given, when
        Product product1 = ProductFixture.gluetenFreeProduct(board);
        Product product2 = ProductFixture.nonGluetenFreeProduct(board);
        Product product3 = ProductFixture.nonGluetenFreeProduct(board);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        FilterRequest filterRequest = FilterRequest.builder()
                .glutenFreeTag(true)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("highProtein 제품이 포함된 게시물만 조회한다.")
    void showListFilterByHighProtein() {
        //given, when
        Product product1 = ProductFixture.highProteinProduct(board);
        Product product2 = ProductFixture.highProteinProduct(board);
        Product product3 = ProductFixture.nonHighProteinProduct(board2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest
                .builder()
                .highProteinTag(true)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("sugarFree 제품이 포함된 게시물만 조회한다.")
    void showListFilterBySugarFree() {
        //given, when
        Product product1 = ProductFixture.sugarFreeProduct(board);
        Product product2 = ProductFixture.sugarFreeProduct(board);
        Product product3 = ProductFixture.nonSugarFreeProduct(board);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
                .sugarFreeTag(true)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("veganFree 제품이 포함된 게시물만 조회한다.")
    void showListFilterByVeganFree() {
        //given, when
        Product product1 = ProductFixture.veganFreeProduct(board);
        Product product2 = ProductFixture.veganFreeProduct(board);
        Product product3 = ProductFixture.nonVeganFreeProduct(board2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
                .veganTag(true)
                .build();
        CursorPageResponse<BoardResponse>boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("ketogenic 제품이 포함된 게시물만 조회한다.")
    void showListFilterKetogenic() {
        //given, when
        Product product1 = ProductFixture.ketogenicProduct(board);
        Product product2 = ProductFixture.ketogenicProduct(board);
        Product product3 = ProductFixture.ketogenicProduct(board2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
                .ketogenicTag(true)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(2);

    }

    @ParameterizedTest
    @EnumSource(value = Category.class)
    @DisplayName("카테고리로 필터링하여서 조회한다.")
    void showListFilterCategory(Category category) {
        //given
        if (category == Category.ALL_BREAD || category == Category.ALL_SNACK) {
            return;
        }
        Product product1 = ProductFixture.categoryBasedProduct(board, category);
        Product product2 = ProductFixture.categoryBasedProduct(board2, Category.ETC);
        Product product3 = ProductFixture.categoryBasedProduct(board2, Category.ETC);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        //when
        FilterRequest filterRequest = FilterRequest.builder()
                .category(category)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        if (category.equals(Category.ETC)) {
            assertThat(boardList.getContent()).hasSize(2);
            return;
        }

        if (category.equals(Category.ALL)) {
            assertThat(boardList.getContent()).hasSize(2);
            return;
        }
        assertThat(boardList.getContent()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bread", "school", "SOCCER", "잼"})
    @DisplayName("잘못된 카테고리로 조회할 경우 예외가 발생한다.")
    void showListFilterWithInvalidCategory(String category) {
        //given, when
        Product product1 = ProductFixture.randomProduct(board);
        Product product2 = ProductFixture.randomProduct(board2);
        Product product3 = ProductFixture.randomProduct(board2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        //then
        Assertions.assertThatThrownBy(() -> FilterRequest.builder()
                        .category(Category.valueOf(category))
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Category.class)
    @DisplayName("성분과 카테고리를 한꺼번에 요청 시 정상적으로 필터링해서 반환한다.")
    void showListFilterCategoryAndIngredient(Category category) {
        //given, when
        if (category == Category.ALL_BREAD || category == Category.ALL_SNACK) {
            return;
        }
        Product product1 = ProductFixture.categoryBasedWithSugarFreeProduct(board, category);
        Product product2 = ProductFixture.categoryBasedWithSugarFreeProduct(board, category);
        Product product3 = ProductFixture.categoryBasedWithNonSugarFreeProduct(board, category);
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        FilterRequest filterRequest = FilterRequest.builder()
                .sugarFreeTag(true)
                .category(category)
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("가격 필터를 적용 시 그에 맞춰 작동한다.")
    void showListFilterPrice() {
        //given, when
        Product product1 = ProductFixture.randomProduct(board);
        Product product2 = ProductFixture.randomProduct(board);
        Product product3 = ProductFixture.randomProduct(board2);
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        FilterRequest filterRequest = FilterRequest.builder()
                .minPrice(5000)
                .build();
        CursorPageResponse<BoardResponse> boardList =
                boardService.getBoards(filterRequest, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest2 = FilterRequest.builder()
                .minPrice(1000)
                .build();
        CursorPageResponse<BoardResponse> boardList2 =
                boardService.getBoards(filterRequest2, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest3 = FilterRequest.builder()
                .maxPrice(10000)
                .build();
        CursorPageResponse<BoardResponse> boardList3 =
                boardService.getBoards(filterRequest3, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest4 = FilterRequest.builder()
                .maxPrice(1000)
                .build();
        CursorPageResponse<BoardResponse> boardList4 =
                boardService.getBoards(filterRequest4, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest5 = FilterRequest.builder()
                .maxPrice(900)
                .build();
        CursorPageResponse<BoardResponse> boardList5 =
                boardService.getBoards(filterRequest5, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest6 = FilterRequest.builder()
                .minPrice(1000)
                .maxPrice(10000)
                .build();
        CursorPageResponse<BoardResponse> boardList6 =
                boardService.getBoards(filterRequest6, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest7 = FilterRequest.builder()
                .minPrice(1001)
                .maxPrice(9999)
                .build();
        CursorPageResponse<BoardResponse> boardList7 =
                boardService.getBoards(filterRequest7, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest8 = FilterRequest.builder()
                .build();
        CursorPageResponse<BoardResponse> boardList8 =
                boardService.getBoards(filterRequest8, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
        assertThat(boardList2.getContent()).hasSize(2);
        assertThat(boardList3.getContent()).hasSize(2);
        assertThat(boardList4.getContent()).hasSize(1);
        assertThat(boardList5.getContent()).isEmpty();
        assertThat(boardList6.getContent()).hasSize(2);
        assertThat(boardList7.getContent()).isEmpty();
        assertThat(boardList8.getContent()).hasSize(2);
    }


    @Test
    @DisplayName("10개 단위로 정상적인 페이지네이션 후 반환한다.")
    void pageTest() {
        //given, when
        Product product1 = ProductFixture.randomProduct(board);
        Product product2 = ProductFixture.randomProduct(board2);
        Product product3 = ProductFixture.randomProduct(board2);

        for (int i = 0; i < 12; i++) {
            board = BoardFixture.randomBoard(store);
            Board newSavedBoard = boardRepository.save(board);
            boardStatisticRepository.save(
                    BoardStatisticFixture.newBoardStatistic(newSavedBoard)
            );

            Product product4 = ProductFixture.randomProduct(board);
            Product product5 = ProductFixture.randomProduct(board);
            productRepository.save(product4);
            productRepository.save(product5);
        }

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
                .build();
        CursorPageResponse<BoardResponse> boardList = boardService.getBoards(filterRequest,
                DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(10);
    }

    @Nested
    @DisplayName("폴더 안의 게시글 조회 테스트")
    class BoardInFolder {

        private static final FolderBoardSortType DEFAULT_SORT_TYPE = FolderBoardSortType.WISHLIST_RECENT;
        private static final Long DEFAULT_CURSOR_ID = null;
        private static final Long DEFAULT_FOLDER_ID = 0L;

        Long memberId;
        WishListFolder wishListFolder;
        Long lastSavedId;
        Long firstSavedId;

        @BeforeEach
        void setup() {
            Member member = MemberFixture.createKakaoMember();
            memberId = memberService.getFirstJoinedMember(member);
            Store store = StoreFixture.storeGenerator();
            store = storeRepository.save(store);

            wishListFolder = wishListFolderRepository.findByMemberId(memberId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));

            for (int i = 0; i < 12; i++) {
                Board createdBoard = BoardFixture.randomBoardWithPrice(store, i * 1000);
                createdBoard = boardRepository.save(createdBoard);
                if (i == 0) {
                    firstSavedId = createdBoard.getId();
                }
                if (i == 11) {
                    lastSavedId = createdBoard.getId();
                }
                Product product = ProductFixture.randomProduct(createdBoard);
                productRepository.save(product);
                BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatistic(
                        createdBoard);
                boardStatisticRepository.save(boardStatistic);
                wishListBoardService.wish(memberId, createdBoard.getId(),
                        new WishListBoardRequest(wishListFolder.getId()));
            }
        }

        @Test
        @DisplayName("wishlist 추가 순으로 폴더 내의 찜한 게시글을 조회한다.")
        void getBoardInFolderWithDefaultOrder() {
            // given, when
            CursorPageResponse<BoardResponse> response = boardService.getPostInFolder(
                    memberId,
                    DEFAULT_SORT_TYPE,
                    wishListFolder.getId(),
                    DEFAULT_CURSOR_ID);
            List<BoardResponse> contents = response.getContent();

            // then
            assertThat(contents).hasSize(10);
            for (int i = 0; i < contents.size(); i++) {
                assertThat(contents.get(i)
                        .getBoardId()).isEqualTo(lastSavedId - i);
            }
            assertThat(response.getNextCursor()).isEqualTo(lastSavedId - 10);
        }

        @Test
        @DisplayName("낮은 가격 순으로 폴더 내 찜한 게시글을 조회한다.")
        void getBoardInFolderWithLowPriceOrder() {
            // given, when
            CursorPageResponse<BoardResponse> response = boardService.getPostInFolder(
                    memberId,
                    FolderBoardSortType.LOW_PRICE,
                    wishListFolder.getId(),
                    DEFAULT_CURSOR_ID);
            List<BoardResponse> contents = response.getContent();

            // then
            assertThat(contents).hasSize(10);
            for (int i = 0; i < contents.size(); i++) {
                assertThat(contents.get(i)
                        .getPrice()).isEqualTo(i * 1000);
            }
            assertThat(response.getNextCursor()).isEqualTo(firstSavedId + 10);
        }

        @Test
        @DisplayName("인기 순으로 폴더 내 찜한 게시글을 조회한다.")
        void getBoardInFolderWithPopularOrder() {
            // given, when
            Member member2 = MemberFixture.createKakaoMember();
            member2 = memberRepository.save(member2);
            Long memberId2 = memberService.getFirstJoinedMember(member2);

            CursorPageResponse<BoardResponse> response = boardService.getPostInFolder(
                    memberId,
                    FolderBoardSortType.LOW_PRICE,
                    wishListFolder.getId(),
                    DEFAULT_CURSOR_ID);
            Long targetId = response.getContent()
                    .get(response.getContent()
                            .size() - 1)
                    .getBoardId();

            wishListBoardService.wish(memberId2, targetId,
                    new WishListBoardRequest(DEFAULT_FOLDER_ID));
            updateBoardStatistic.updateStatistic();

            // then
            CursorPageResponse<BoardResponse> responseAfterWish = boardService.getPostInFolder(
                    memberId,
                    FolderBoardSortType.POPULAR,
                    wishListFolder.getId(),
                    DEFAULT_CURSOR_ID);
            List<BoardResponse> contents = responseAfterWish.getContent();

            assertThat(contents).hasSize(10);
            assertThat(contents.stream()
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new)
                    .getBoardId()).isEqualTo(targetId);
        }
    }

    @Nested
    @DisplayName("getBoardDtos 메서드는")
    @Transactional
    class GetBoardDtos {

        Board targetBoard;
        final Long memberId = null;
        final String TEST_URL = "www.TESTURL.com";
        final Long NOT_EXSIST_ID = -1L;
        @Value("${cdn.domain}")
        private String cdn;

        @BeforeEach
        void init() {
            ProductImg productImg = productImgRepository.save(fixtureBoardImage(Map.of("url", TEST_URL)));
            BoardDetail boardDetail = fixtureBoardDetail(Map.of("imgIndex", productImg.getId().intValue(), "url", TEST_URL));
            targetBoard = boardRepository.save(fixtureBoard(Map.of("boardDetails", List.of(boardDetail))));
            productImg.updateBoard(targetBoard);
        }

        @Test
        @DisplayName("유효한 boardId로 게시판, 게시판 이미지, 게시판 상세 정보 이미지 조회할 수 있다")
        void getProductResponseTest() {
            String viewKey = "viewKey";
            BoardImageDetailResponse boardDtos = boardDetailService.getBoardDtos(memberId,
                    targetBoard.getId(), viewKey);

            assertThat(boardDtos.getBoardImages()).hasSize(1);
            assertThat(boardDtos.getBoardDetail()).hasSize(1);

            String boardImageUrl = boardDtos.getBoardImages()
                    .stream()
                    .findFirst()
                    .get();

            String cdnWithFilePath = cdn + TEST_URL;
            assertThat(boardImageUrl).isEqualTo(cdnWithFilePath);
        }
    }

    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class FindProductDtoById {

        private Member testMember;
        private Board testBoard;
        private Product testProduct;
        private Push testPush;

        @BeforeEach
        void setUp() {
            // Given: 테스트 데이터를 세팅합니다.
            testMember = memberRepository.save(MemberFixture.createKakaoMember());

            testBoard = fixtureBoard(Collections.emptyMap());

            testProduct = Product.builder()
                    .title("Sample Product")
                    .price(1000)
                    .category(Category.COOKIE) // 실제 Category 설정
                    .glutenFreeTag(true)
                    .highProteinTag(true)
                    .sugarFreeTag(true)
                    .veganTag(true)
                    .ketogenicTag(true)
                    .nutrition(new Nutrition(
                            200, 200, 15,
                            10, 5, 3, 500))
                    .monday(true)
                    .tuesday(true)
                    .wednesday(true)
                    .thursday(true)
                    .friday(true)
                    .saturday(true)
                    .sunday(true)
                    .orderStartDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                    .orderEndDate(LocalDateTime.of(2024, 1, 7, 23, 59))
                    .soldout(false)
                    .board(testBoard)
                    .build();

            productRepository.save(testProduct);

            testPush = Push.builder()
                    .productId(testProduct.getId())
                    .memberId(testMember.getId())
                    .pushType(PushType.DATE) // 실제 PushType 설정
                    .days("Monday,Friday")
                    .isActive(true)
                    .build();

            pushRepository.save(testPush);
        }
    }
}
