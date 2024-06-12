package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.AbstractIntegrationTest;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class BoardServiceTest extends AbstractIntegrationTest {

    private static final Long NULL_CURSOR = null;
    private static final SortType DEFAULT_SORT_TYPE = SortType.RECOMMEND;
    private static final Long NULL_MEMBER = null;

    private static final String TEST_TITLE = "TestTitle";

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    RankingRepository rankingRepository;

    @Autowired
    BoardService boardService;

    @Autowired
    WishListBoardService wishListBoardService;

    @Autowired
    EntityManager entityManager;

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

        Board save1 = boardRepository.save(board);
        Board save2 = boardRepository.save(board2);
        boardRepository.save(board2);

        rankingRepository.save(
            RankingFixture.newRanking(save1)
        );
        rankingRepository.save(
            RankingFixture.newRanking(save2)
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

        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        BoardResponseDto response1 = boardList.getContent()
            .get(0);
        BoardResponseDto response2 = boardList.getContent()
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(2);

    }

    @ParameterizedTest
    @EnumSource(value = Category.class)
    @DisplayName("카테고리로 필터링하여서 조회한다.")
    void showListFilterCategory(Category category) {
        //given
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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
        BoardCustomPage<List<BoardResponseDto>> boardList =
            boardService.getBoardList(filterRequest, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest2 = FilterRequest.builder()
            .minPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList2 =
            boardService.getBoardList(filterRequest2, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest3 = FilterRequest.builder()
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList3 =
            boardService.getBoardList(filterRequest3, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest4 = FilterRequest.builder()
            .maxPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList4 =
            boardService.getBoardList(filterRequest4, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest5 = FilterRequest.builder()
            .maxPrice(900)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList5 =
            boardService.getBoardList(filterRequest5, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest6 = FilterRequest.builder()
            .minPrice(1000)
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList6 =
            boardService.getBoardList(filterRequest6, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest7 = FilterRequest.builder()
            .minPrice(1001)
            .maxPrice(9999)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList7 =
            boardService.getBoardList(filterRequest7, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest8 = FilterRequest.builder()
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList8 =
            boardService.getBoardList(filterRequest8, DEFAULT_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            rankingRepository.save(
                RankingFixture.newRanking(newSavedBoard)
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
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
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

        Member member;
        WishListFolder wishListFolder;
        Long lastSavedId;
        Long firstSavedId;

        @BeforeEach
        void setup() {
            member = MemberFixture.createKakaoMember();
            member = memberService.getFirstJoinedMember(member);
            Store store = StoreFixture.storeGenerator();
            store = storeRepository.save(store);

            wishListFolder = wishListFolderRepository.findByMemberId(member.getId())
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
                Ranking ranking = RankingFixture.newRanking(createdBoard);
                rankingRepository.save(ranking);
                wishListBoardService.wish(member.getId(), createdBoard.getId(),
                    new WishListBoardRequest(wishListFolder.getId()));
            }
        }

        @Test
        @DisplayName("wishlist 추가 순으로 폴더 내의 찜한 게시글을 조회한다.")
        void getBoardInFolderWithDefaultOrder() {
            // given, when
            BoardCustomPage<List<BoardResponseDto>> response = boardService.getPostInFolder(
                member.getId(),
                DEFAULT_SORT_TYPE,
                wishListFolder.getId(),
                DEFAULT_CURSOR_ID);
            List<BoardResponseDto> contents = response.getContent();

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
            BoardCustomPage<List<BoardResponseDto>> response = boardService.getPostInFolder(
                member.getId(),
                FolderBoardSortType.LOW_PRICE,
                wishListFolder.getId(),
                DEFAULT_CURSOR_ID);
            List<BoardResponseDto> contents = response.getContent();

            // then
            assertThat(contents).hasSize(10);
            for (int i = 0; i < contents.size(); i++) {
                assertThat(contents.get(i).getPrice()).isEqualTo(i * 1000);
            }
            assertThat(response.getNextCursor()).isEqualTo(firstSavedId + 10);
        }

        @Test
        @DisplayName("인기 순으로 폴더 내 찜한 게시글을 조회한다.")
        void getBoardInFolderWithPopularOrder() {
            // given, when
            Member member2 = MemberFixture.createKakaoMember();
            member2 = memberService.getFirstJoinedMember(member2);

            BoardCustomPage<List<BoardResponseDto>> response = boardService.getPostInFolder(
                member.getId(),
                FolderBoardSortType.LOW_PRICE,
                wishListFolder.getId(),
                DEFAULT_CURSOR_ID);
            Long targetId = response.getContent()
                .get(response.getContent().size() - 1)
                .getBoardId();

            wishListBoardService.wish(member2.getId(), targetId,
                new WishListBoardRequest(DEFAULT_FOLDER_ID));

            // then
            BoardCustomPage<List<BoardResponseDto>> responseAfterWish = boardService.getPostInFolder(
                member.getId(),
                FolderBoardSortType.POPULAR,
                wishListFolder.getId(),
                DEFAULT_CURSOR_ID);
            List<BoardResponseDto> contents = responseAfterWish.getContent();

            assertThat(contents).hasSize(10);
            assertThat(contents.stream().findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getBoardId()).isEqualTo(targetId);
        }

        @Nested
        @DisplayName("getTopBoardInfo 메서드는")
        class GetTopBoardInfo {

            private Store store;
            private Ranking firstRanking;
            private Ranking secondRanking;
            private Ranking thirdRanking;
            private Member member;

            @BeforeEach
            void init() {
                store = fixtureStore(emptyMap());

                secondRanking = fixtureRanking(
                    Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore",
                        101d)); // 2등
                firstRanking = fixtureRanking(
                    Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore",
                        102d)); // 1등
                thirdRanking = fixtureRanking(
                    Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore",
                        100d)); // 3등

                createWishListStore();
            }

            @Test
            @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
            void getPopularBoard() {
                List<PopularBoardResponse> topBoardInfo = boardService.getTopBoardInfo(NULL_MEMBER,
                    store.getId());

                AssertionsForInterfaceTypes.assertThat(topBoardInfo).hasSize(3);

                List<Long> actualBoardIds = topBoardInfo.stream()
                    .map(PopularBoardResponse::getBoardId).toList();
                List<Long> expectBoardIds = List.of(
                    firstRanking.getBoard().getId(),
                    secondRanking.getBoard().getId(),
                    thirdRanking.getBoard().getId());

                AssertionsForInterfaceTypes.assertThat(actualBoardIds)
                    .containsExactlyElementsOf(expectBoardIds);
            }

            @Test
            @DisplayName("위시리스트 등록한 상품을 정상적으로 가져올 가져올 수 있다")
            void getIsWished() {
                List<PopularBoardResponse> topBoardInfo = boardService.getTopBoardInfo(
                    member.getId(),
                    store.getId());

                Boolean wishTrue = topBoardInfo.stream()
                    .filter(board -> board.getBoardId().equals(firstRanking.getBoard().getId()))
                    .findFirst()
                    .get()
                    .getIsWished();

                Boolean wishFalse = topBoardInfo.stream()
                    .filter(board -> board.getBoardId().equals(secondRanking.getBoard().getId()))
                    .findFirst()
                    .get()
                    .getIsWished();

                AssertionsForClassTypes.assertThat(wishTrue).isTrue();
                AssertionsForClassTypes.assertThat(wishFalse).isFalse();
            }

            void createWishListStore() {
                member = memberRepository.save(Member.builder().build());

                wishListBoardRepository.save(WishListBoard.builder()
                    .boardId(firstRanking.getBoard().getId())
                    .memberId(member.getId())
                    .build());
            }
        }
    }
    @Nested
    @DisplayName("getBoardDtos 메서드는")
    class GetBoardDtos {

        Board targetBoard;
        final Long memberId = null;
        final String TEST_URL = "www.TESTURL.com";
        final Long NOT_EXSIST_ID = -1L;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of("title", TEST_TITLE));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
            fixtureBoardDetail(Map.of("board", targetBoard, "url", TEST_URL));
            fixtureBoardDetail(Map.of("board", targetBoard, "url", TEST_URL));
        }

        @Test
        @DisplayName("유효한 boardId로 게시판, 게시판 이미지, 게시판 상세 정보 이미지 조회할 수 있다")
        void getProductResponseTest() {
            BoardImageDetailResponse boardDtos = boardService.getBoardDtos(memberId,
                targetBoard.getId());

            assertThat(boardDtos.getBoardImages()).hasSize(2);
            assertThat(boardDtos.getBoardDetails()).hasSize(2);

            String boardDetailUrl = boardDtos.getBoardDetails()
                .stream()
                .findFirst()
                .get();

            String boardImageUrl = boardDtos.getBoardImages()
                .stream()
                .findFirst()
                .get();

            assertThat(boardDetailUrl).isEqualTo(TEST_URL);
            assertThat(boardImageUrl).isEqualTo(TEST_URL);
        }

        @Test
        @DisplayName("유효하지 않은 boardId로 조회 시 BbangleException을 발생시킨다")
        void throwNotBoard() {
            assertThrows(BbangleException.class,
                () -> boardService.getProductResponse(NOT_EXSIST_ID));
        }
    }


    @Nested
    @DisplayName("getProductResponse 메서드는")
    class GetProductResponse {

        List<Product> products;
        Board targetBoard;
        final Long NOT_EXSIST_ID = -1L;

        @BeforeEach
        void init() {
            products = List.of(
                fixtureProduct(Map.of(
                    "title", TEST_TITLE,
                    "category", Category.BREAD
                )),
                fixtureProduct(Map.of(
                    "title", TEST_TITLE,
                    "category", Category.SNACK
                )));

            targetBoard = fixtureBoard(Map.of("productList", products));
        }

        @Test
        @DisplayName("유효한 boardId로 상품리스트를 조회할 수 있다")
        void getProductResponseTest() {
            ProductResponse productResponse = boardService.getProductResponse(targetBoard.getId());
            List<ProductDto> productList = productResponse.getProducts();

            assertThat(productList).hasSize(2);
            assertThat(productResponse.getBoardIsBundled()).isTrue();
            productList.forEach(productDto -> {
                assertThat(productDto.getId()).isNotNull();
                assertThat(productDto.getTitle()).isNotNull();
            });
        }

        @Test
        @DisplayName("유효하지 않은 boardId로 조회 시 BbangleException을 발생시킨다")
        void throwNotBoard() {
            assertThrows(BbangleException.class,
                () -> boardService.getProductResponse(NOT_EXSIST_ID));
        }
    }
}
