package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoardServiceTest extends AbstractIntegrationTest {

    private static final CursorInfo NULL_CURSOR = null;
    private static final SortType NULL_SORT_TYPE = null;
    private static final Long NULL_MEMBER = null;

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
    EntityManager entityManager;

    Board board;
    Board board2;
    Store store;
    Store store2;

    @BeforeEach
    void setup() {
        rankingRepository.deleteAll();
        productRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        if(category.equals(Category.ETC)){
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
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

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
            boardService.getBoardList(filterRequest, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest2 =  FilterRequest.builder()
            .minPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList2 =
            boardService.getBoardList(filterRequest2, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest3 = FilterRequest.builder()
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList3 =
            boardService.getBoardList(filterRequest3, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest4 = FilterRequest.builder()
            .maxPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList4 =
            boardService.getBoardList(filterRequest4, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest5 = FilterRequest.builder()
            .maxPrice(900)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList5 =
            boardService.getBoardList(filterRequest5, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest6 = FilterRequest.builder()
            .minPrice(1000)
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList6 =
            boardService.getBoardList(filterRequest6, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest7 = FilterRequest.builder()
            .minPrice(1001)
            .maxPrice(9999)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList7 =
            boardService.getBoardList(filterRequest7, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);
        FilterRequest filterRequest8 = FilterRequest.builder().build();
        BoardCustomPage<List<BoardResponseDto>> boardList8 =
            boardService.getBoardList(filterRequest8, NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(1);
        assertThat(boardList2.getContent()).hasSize(2);
        assertThat(boardList3.getContent()).hasSize(2);
        assertThat(boardList4.getContent()).hasSize(1);
        assertThat(boardList5.getContent()).hasSize(0);
        assertThat(boardList6.getContent()).hasSize(2);
        assertThat(boardList7.getContent()).hasSize(0);
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
        FilterRequest filterRequest = FilterRequest.builder().build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR, NULL_MEMBER);

        //then
        assertThat(boardList.getContent()).hasSize(10);
        assertThat(boardList.getBoardCount()).isEqualTo(14);
    }

}
