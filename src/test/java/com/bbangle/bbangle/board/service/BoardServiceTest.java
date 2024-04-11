package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class BoardServiceTest {

    private static final Long NULL_CURSOR = null;
    private static final SortType NULL_SORT_TYPE = null;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BoardService boardService;

    Board board;
    Board board2;
    Store store;
    Store store2;
    PageRequest defaultPage;

    @BeforeEach
    void setup() {
        defaultPage = PageRequest.of(0, 10);

        store = storeGenerator();
        storeRepository.save(store);
        store2 = storeGenerator();
        storeRepository.save(store2);

        board = boardGenerator(store,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            1000);

        board2 = boardGenerator(store,
            true,
            false,
            true,
            true,
            false,
            true,
            true,
            10000);
        boardRepository.save(board);
        boardRepository.save(board2);
    }


    @ParameterizedTest
    @NullSource
    @DisplayName("모든 리스트를 정상적으로 조회한다. 필터가 없을 경우")
    public void showAllList(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            true,
            true,
            true,
            true,
            true,
            "BREAD");

        Product product2 = productGenerator(board2,
            false,
            true,
            true,
            true,
            false,
            "BREAD");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            false,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
            .build();

        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);
        BoardResponseDto response1 = boardList.getContent()
            .get(0);
        BoardResponseDto response2 = boardList.getContent()
            .get(1);

        //then
        assertThat(boardList.getContent()).hasSize(2);

        assertThat(response1.tags()
            .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.VEGAN.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.KETOGENIC.label())).isEqualTo(true);
        assertThat(response2.tags()
            .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(true);
        assertThat(response2.tags()
            .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(true);
        assertThat(response2.tags()
            .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response2.tags()
            .contains(TagEnum.VEGAN.label())).isEqualTo(true);
        assertThat(response2.tags()
            .contains(TagEnum.KETOGENIC.label())).isEqualTo(false);

    }

    @ParameterizedTest
    @NullSource
    @DisplayName("glutenFree 제품이 포함된 게시물만 조회한다.")
    public void showListFilterByGlutenFree(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            true,
            true,
            true,
            true,
            false,
            "BREAD");

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            true,
            false,
            "BREAD");

        Product product3 = productGenerator(board2,
            false,
            false,
            true,
            false,
            false,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        FilterRequest filterRequest = FilterRequest.builder()
            .orderAvailableToday(true)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);
        BoardResponseDto response1 = boardList.getContent()
            .get(0);

        //then
        assertThat(boardList.getContent()).hasSize(1);

        assertThat(response1.tags()
            .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.VEGAN.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.KETOGENIC.label())).isEqualTo(false);

    }

    @ParameterizedTest
    @NullSource
    @DisplayName("highProtein 제품이 포함된 게시물만 조회한다.")
    public void showListFilterByHighProtein(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            true,
            false,
            "BREAD");

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            true,
            false,
            "BREAD");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = new FilterRequest(true,
            noFilter,
            noFilter,
            noFilter, noFilter, null, null, null, null);
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            null, null);

        //then
        assertThat(boardList.getContent()).hasSize(1);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("sugarFree 제품이 포함된 게시물만 조회한다.")
    public void showListFilterBySugarFree(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            true,
            false,
            "BREAD");

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            true,
            false,
            "BREAD");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = new FilterRequest(true,
            noFilter,
            noFilter,
            noFilter, noFilter, null, null, null, null);
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            null, null);

        //then
        assertThat(boardList.getContent()).hasSize(1);

    }

    @ParameterizedTest
    @NullSource
    @DisplayName("veganFre 제품이 포함된 게시물만 조회한다.")
    public void showListFilterByVeganFree(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            false,
            false,
            "BREAD");

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            false,
            false,
            "BREAD");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);

        //then
        assertThat(boardList.getContent()).hasSize(0);

    }

    @ParameterizedTest
    @NullSource
    @DisplayName("ketogenic 제품이 포함된 게시물만 조회한다.")
    public void showListFilterKetogenic(Boolean noFilter) {
        //given, when

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            false,
            true,
            "BREAD");

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            false,
            true,
            "BREAD");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "BREAD");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);

        //then
        assertThat(boardList.getContent()).hasSize(2);

    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT"})
    @DisplayName("카테고리로 필터링하여서 조회한다.")
    public void showListFilterCategory(String category) {
        //given

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            false,
            true,
            category);

        Product product2 = productGenerator(board2,
            false,
            true,
            true,
            false,
            true,
            "ETC");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "ETC");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        Category realCategory;
        if (category != null & !category.isBlank()) {
            realCategory = Category.valueOf("ETC");
        } else {
            realCategory = Category.valueOf(category);
        }

        //when
        FilterRequest filterRequest = FilterRequest.builder()
            .category(realCategory)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);

        //then
        assertThat(boardList.getContent()).hasSize(1);

    }

    @ParameterizedTest
    @ValueSource(strings = {"bread", "school", "SOCCER", "잼"})
    @DisplayName("잘못된 카테고리로 조회할 경우 예외가 발생한다.")
    public void showListFilterWithInvalidCategory(String category) {
        //given, when

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            false,
            true,
            "BREAD");

        Product product2 = productGenerator(board2,
            false,
            true,
            true,
            false,
            true,
            "ETC");

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true,
            "JAM");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        //then
        Assertions.assertThatThrownBy(() -> FilterRequest.builder()
                .category(Category.valueOf(category))
                .build())
            .isInstanceOf(BbangleException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT"})
    @DisplayName("성분과 카테고리를 한꺼번에 요청 시 정상적으로 필터링해서 반환한다.")
    public void showListFilterCategoryAndIngredient(String category) {
        //given, when

        Product product1 = productGenerator(board,
            true,
            true,
            false,
            true,
            true,
            category);

        Product product2 = productGenerator(board2,
            false,
            false,
            false,
            false,
            true,
            category);

        Product product3 = productGenerator(board2,
            false,
            false,
            true,
            false,
            true,
            category);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        FilterRequest filterRequest = FilterRequest.builder()
            .sugarFreeTag(true)
            .category(Category.valueOf(category))
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);
        BoardResponseDto response1 = boardList.getContent()
            .get(0);

        //then
        assertThat(boardList.getContent()).hasSize(1);

        assertThat(response1.tags()
            .contains(TagEnum.GLUTEN_FREE.label())).isEqualTo(false);
        assertThat(response1.tags()
            .contains(TagEnum.HIGH_PROTEIN.label())).isEqualTo(false);
        assertThat(response1.tags()
            .contains(TagEnum.SUGAR_FREE.label())).isEqualTo(true);
        assertThat(response1.tags()
            .contains(TagEnum.VEGAN.label())).isEqualTo(false);
        assertThat(response1.tags()
            .contains(TagEnum.KETOGENIC.label())).isEqualTo(true);

    }

    @Test
    @DisplayName("가격 필터를 적용 시 그에 맞춰 작동한다.")
    public void showListFilterPrice() {
        //given, when
        FilterRequest filterRequest = FilterRequest.builder()
            .minPrice(5000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList =
            boardService.getBoardList(filterRequest, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest2 =  FilterRequest.builder()
            .minPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList2 =
            boardService.getBoardList(filterRequest2, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest3 = FilterRequest.builder()
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList3 =
            boardService.getBoardList(filterRequest3, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest4 = FilterRequest.builder()
            .maxPrice(1000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList4 =
            boardService.getBoardList(filterRequest4, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest5 = FilterRequest.builder()
            .maxPrice(900)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList5 =
            boardService.getBoardList(filterRequest5, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest6 = FilterRequest.builder()
            .minPrice(1000)
            .maxPrice(10000)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList6 =
            boardService.getBoardList(filterRequest6, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest7 = FilterRequest.builder()
            .minPrice(1001)
            .maxPrice(9999)
            .build();
        BoardCustomPage<List<BoardResponseDto>> boardList7 =
            boardService.getBoardList(filterRequest7, NULL_SORT_TYPE, NULL_CURSOR);
        FilterRequest filterRequest8 = FilterRequest.builder().build();
        BoardCustomPage<List<BoardResponseDto>> boardList8 =
            boardService.getBoardList(filterRequest8, NULL_SORT_TYPE, NULL_CURSOR);

        //then
        assertThat(boardList.getContent()).hasSize(1);
        assertThat(boardList2.getContent()).hasSize(2);
        assertThat(boardList3.getContent()).hasSize(2);
        assertThat(boardList4.getContent()).hasSize(1);
        assertThat(boardList5.getContent()).hasSize(0);
        assertThat(boardList6.getContent()).hasSize(2);
        assertThat(boardList7.getContent()).hasSize(0);

    }


    @Test
    @DisplayName("성분과 카테고리를 한꺼번에 요청 시 정상적으로 필터링해서 반환한다.")
    public void SliceTest() {
        //given, when

        Product product1 = productGenerator(board,
            true,
            true,
            false,
            true,
            true,
            Category.BREAD.name());

        Product product2 = productGenerator(board2,
            false,
            false,
            false,
            false,
            true,
            Category.BREAD.name());

        Product product3 = productGenerator(board2,
            false,
            false,
            true,
            false,
            true,
            Category.BREAD.name());

        for (int i = 0; i < 12; i++) {
            board = boardGenerator(store,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                1000);
            boardRepository.save(board);

            Product product5 = productGenerator(board2,
                false,
                false,
                false,
                false,
                true,
                Category.BREAD.name());

            Product product4 = productGenerator(board,
                false,
                false,
                true,
                false,
                true,
                Category.BREAD.name());
            productRepository.save(product5);
            productRepository.save(product4);
        }

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        List<Board> all = boardRepository.findAll();
        FilterRequest filterRequest = FilterRequest.builder().build();
        BoardCustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(filterRequest,
            NULL_SORT_TYPE, NULL_CURSOR);

        //then
        assertThat(boardList.getContent()).hasSize(10);
    }

    private Board boardGenerator(
        Store store,
        boolean sunday,
        boolean monday,
        boolean tuesday,
        boolean wednesday,
        boolean thursday,
        boolean friday,
        boolean saturday,
        int price
    ) {
        return Board.builder()
            .store(store)
            .title("title")
            .price(price)
            .status(true)
            .profile("profile")
            .purchaseUrl("purchaseUrl")
            .view(1)
            .sunday(sunday)
            .monday(monday)
            .tuesday(tuesday)
            .wednesday(wednesday)
            .thursday(thursday)
            .friday(friday)
            .saturday(saturday)
            .isDeleted(sunday)
            .build();
    }

    private Store storeGenerator() {
        return Store.builder()
            .identifier("identifier")
            .name("name")
            .introduce("introduce")
            .profile("profile")
            .isDeleted(false)
            .build();
    }

    private Product productGenerator(
        Board board,
        boolean glutenFreeTag,
        boolean highProteinTag,
        boolean sugarFreeTag,
        boolean veganTag,
        boolean ketogenicTag,
        String category
    ) {
        return Product.builder()
            .board(board)
            .title("title")
            .price(1000)
            .category(Category.valueOf(category))
            .glutenFreeTag(glutenFreeTag)
            .highProteinTag(highProteinTag)
            .sugarFreeTag(sugarFreeTag)
            .veganTag(veganTag)
            .ketogenicTag(ketogenicTag)
            .build();
    }
}
