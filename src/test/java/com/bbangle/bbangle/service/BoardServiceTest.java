package com.bbangle.bbangle.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import com.bbangle.bbangle.dto.BoardResponseDto;
import com.bbangle.bbangle.model.Board;
import com.bbangle.bbangle.model.Category;
import com.bbangle.bbangle.model.Product;
import com.bbangle.bbangle.model.Store;
import com.bbangle.bbangle.repository.BoardRepository;
import com.bbangle.bbangle.repository.ProductRepository;
import com.bbangle.bbangle.repository.StoreRepository;
import org.hibernate.annotations.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();

        Store store = storeGenerator();
        storeRepository.save(store);

        board = boardGenerator(store,
            true,
            true,
            true,
            true,
            true,
            true,
            true);

        board2 = boardGenerator(store,
            true,
            false,
            true,
            true,
            false,
            true,
            true);
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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, noFilter, noFilter, noFilter,
            noFilter, noFilter, noSort);
        BoardResponseDto response1 = boardList.get(0);
        BoardResponseDto response2 = boardList.get(1);

        //then
        assertThat(boardList).hasSize(2);

        assertThat(response1.tagDto().glutenFreeTag()).isEqualTo(true);
        assertThat(response1.tagDto().highProteinTag()).isEqualTo(true);
        assertThat(response1.tagDto().sugarFreeTag()).isEqualTo(true);
        assertThat(response1.tagDto().veganTag()).isEqualTo(true);
        assertThat(response1.tagDto().ketogenicTag()).isEqualTo(true);

        assertThat(response2.tagDto().glutenFreeTag()).isEqualTo(true);
        assertThat(response2.tagDto().highProteinTag()).isEqualTo(true);
        assertThat(response2.tagDto().sugarFreeTag()).isEqualTo(true);
        assertThat(response2.tagDto().veganTag()).isEqualTo(true);
        assertThat(response2.tagDto().ketogenicTag()).isEqualTo(false);

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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, true, noFilter, noFilter,
            noFilter, noFilter, noSort);
        BoardResponseDto response1 = boardList.get(0);

        //then
        assertThat(boardList).hasSize(1);

        assertThat(response1.tagDto().glutenFreeTag()).isEqualTo(true);
        assertThat(response1.tagDto().highProteinTag()).isEqualTo(true);
        assertThat(response1.tagDto().sugarFreeTag()).isEqualTo(true);
        assertThat(response1.tagDto().veganTag()).isEqualTo(true);
        assertThat(response1.tagDto().ketogenicTag()).isEqualTo(false);

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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, true, noFilter, noFilter,
            noFilter, noFilter, noSort);

        //then
        assertThat(boardList).hasSize(1);
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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, noFilter, true, noFilter,
            noFilter, noFilter, noSort);

        //then
        assertThat(boardList).hasSize(1);

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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, noFilter, noFilter, noFilter,
            true, noFilter, noSort);

        //then
        assertThat(boardList).hasSize(0);

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

        Product product3 =  productGenerator(board2,
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
        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, noFilter, noFilter, true,
            noFilter, noFilter, noSort);

        //then
        assertThat(boardList).hasSize(2);

    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT"})
    @DisplayName("ketogenic 제품이 포함된 게시물만 조회한다.")
    public void showListFilterCategory(String category) {
        //given, when

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

        Product product3 =  productGenerator(board2,
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
        if(category != null & !category.isBlank()){
            realCategory = "ETC";
        } else {
            realCategory = category;
        }

        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, null, null, null,
            null, null, realCategory);

        //then
        assertThat(boardList).hasSize(1);

    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT"})
    @DisplayName("ketogenic 제품이 포함된 게시물만 조회한다.")
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

        Product product3 =  productGenerator(board2,
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

        List<BoardResponseDto> boardList = boardService.getBoardList(noSort, null, null, true,
            null, null, category);
        BoardResponseDto response1 = boardList.get(0);

        //then
        assertThat(boardList).hasSize(1);

        assertThat(response1.tagDto().glutenFreeTag()).isEqualTo(false);
        assertThat(response1.tagDto().highProteinTag()).isEqualTo(false);
        assertThat(response1.tagDto().sugarFreeTag()).isEqualTo(true);
        assertThat(response1.tagDto().veganTag()).isEqualTo(false);
        assertThat(response1.tagDto().ketogenicTag()).isEqualTo(true);

    }


    private Board boardGenerator(Store store,
                                 boolean sunday,
                                 boolean monday,
                                 boolean tuesday,
                                 boolean wednesday,
                                 boolean thursday,
                                 boolean friday,
                                 boolean saturday) {
        return Board.builder()
            .store(store)
            .title("title")
            .price(1000)
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

    private Product productGenerator(Board board,
                                     boolean glutenFreeTag,
                                     boolean highProteinTag,
                                     boolean sugarFreeTag,
                                     boolean veganTag,
                                     boolean ketogenicTag,
                                     String category) {
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