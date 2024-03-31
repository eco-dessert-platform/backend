package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.exception.CategoryTypeException;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
public class BoardServiceTest {

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
        productRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();

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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, noFilter, noFilter,
            noFilter,
            noFilter, noFilter, noSort, null, null, null, null, null);
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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, true, noFilter,
            noFilter,
            noFilter, noFilter, noSort, null, null, null, null, null);
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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, true, noFilter,
            noFilter,
            noFilter, noFilter, noSort, null, null, null, null, null);

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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, true, noFilter,
            noFilter,
            noFilter, noFilter, noSort, null, null, null, null, null);

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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, noFilter, noFilter,
            noFilter,
            true, noFilter, noSort, null, null, null, null, null);

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
        String noSort = "";
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, noFilter, noFilter,
            noFilter,
            noFilter, true, noSort, null, null, null, null, null);

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
        String noSort = "";

        String realCategory;
        if (category != null & !category.isBlank()) {
            realCategory = "ETC";
        } else {
            realCategory = category;
        }

        //when
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, null, null, null,
            null, null, category, null, null, null, null, null);

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
        String noSort = "";

        //then
        Assertions.assertThatThrownBy(() -> boardService.getBoardList(noSort, null, null, null,
                null, null, category, null, null, null, null, null))
            .isInstanceOf(CategoryTypeException.class);

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
        String noSort = "";

        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, null, null, true,
            null, null, category, null, null, null, null, null);
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

        CustomPage<List<BoardResponseDto>> boardList =
            boardService.getBoardList(null, null, null, null, null, null, null, 5000, null,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList2 =
            boardService.getBoardList(null, null, null, null, null, null, null, 1000, null,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList3 =
            boardService.getBoardList(null, null, null, null, null, null, null, null, 10000,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList4 =
            boardService.getBoardList(null, null, null, null, null, null, null, null, 1000,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList5 =
            boardService.getBoardList(null, null, null, null, null, null, null, null, 900,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList6 =
            boardService.getBoardList(null, null, null, null, null, null, null, 1000, 10000,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList7 =
            boardService.getBoardList(null, null, null, null, null, null, null, 1001, 9999,
                null, null, null);
        CustomPage<List<BoardResponseDto>> boardList8 =
            boardService.getBoardList(null, null, null, null, null, null, null, null, null,
                null, null, null);
        List<Board> all = boardRepository.findAll();

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

        String noSort = "";

        List<Board> all = boardRepository.findAll();
        CustomPage<List<BoardResponseDto>> boardList = boardService.getBoardList(noSort, null, null, null,
            null, null, null, null, null, null, null, null);
        CustomPage<List<BoardResponseDto>> boardList2 = boardService.getBoardList(noSort, null, null, null,
            null, null, null, null, null, null, null, null);

        //then
        assertThat(boardList.getContent()).hasSize(10);
        assertThat(boardList2.getContent()).hasSize(4);

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
            .detail("detail")
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

    @Test
    @DisplayName("오브젝트 스토리지에 상세페이지를 저장할 수 있으며, Board 테이블 detail 컬럼을 최신 값으로 업데이트 할 수 있다")
    public void saveBoardDetailHtmlTest() {
        byte[] content;
        try {
            content = Files.readAllBytes(Paths.get("src/test/resources/html/detail.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // MockMultipartFile 인스턴스 생성
        MockMultipartFile HTML_FILE = new MockMultipartFile(
            "htmlFile", // form의 input field 이름
            "detail.html", // 업로드될 파일명
            "text/html", // 파일 타입
            content // 파일 내용
        );

        Long boardId = 1L;

        var result = boardService.saveBoardDetailHtml(boardId, HTML_FILE);
        Assertions.assertThat(result)
            .isEqualTo(true);
    }


}
