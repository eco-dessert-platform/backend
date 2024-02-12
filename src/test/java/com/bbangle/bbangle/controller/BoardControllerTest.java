package com.bbangle.bbangle.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.model.Board;
import com.bbangle.bbangle.model.Category;
import com.bbangle.bbangle.model.Product;
import com.bbangle.bbangle.model.Store;
import com.bbangle.bbangle.repository.BoardRepository;
import com.bbangle.bbangle.repository.ProductRepository;
import com.bbangle.bbangle.repository.StoreRepository;
import com.bbangle.bbangle.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BoardService boardService;

    @Autowired
    MockMvc mockMvc;

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

        Product product1 = productGenerator(board,
            false,
            false,
            true,
            false,
            true);

        Product product2 = productGenerator(board,
            false,
            true,
            true,
            false,
            true);

        Product product3 = productGenerator(board2,
            true,
            false,
            true,
            false,
            true);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    @Test
    @DisplayName("순서나 필터링 조건이 없어도 정상적으로 조회한다.")
    public void getBoardListSuccessWithoutAnyCondition() throws Exception {
        //given, when, then
        mockMvc.perform(get("/api/v1/boards"))
            .andExpect(status().isOk())
            .andDo(print());

    }

    @ParameterizedTest
    @ValueSource(strings = {"glutenFreeTag", "highProteinTag", "sugarFreeTag", "veganTag", "ketogenicTag"})
    @DisplayName("순서가 없고 필터링 조건이 있어도 정상적으로 조회한다.")
    public void getBoardListSuccessWithIngredientFilteringCondition(String ingredient) throws Exception {
        //given, when, then
        mockMvc.perform(get("/api/v1/boards")
                .param(ingredient, "true"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT", "ETC"})
    @DisplayName("순서가 없고 카테고리 필터링 조건이 있어도 정상적으로 조회한다.")
    public void getBoardListSuccessWithCategoryCondition(String category) throws Exception {
        //given, when, then
        mockMvc.perform(get("/api/v1/boards")
                .param("category", category))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT", "ETC"})
    @DisplayName("순서가 없고 필터링 조건 둘 이상 있어도 정상적으로 조회한다.")
    public void getBoardListSuccessWithCategoryAndIngredientCondition(String ingredient) throws Exception {
        // given
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        info.add(ingredient, "true");
        info.add("category", "BREAD");
        info.add("sort", "latest");

        // when, then
        mockMvc.perform(get("/api/v1/boards")
                .params(info))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"bread", "school", "SOCCER", "잼"})
    @DisplayName("잘못된 카테고리로 카테고리 필터링 검색을 하면 조회한다.")
    public void getBoardListFailWithWrongCategory(String category) throws Exception {
        // given, when, then
        mockMvc.perform(get("/api/v1/boards")
                .param("category", category))
            .andExpect(status().isBadRequest())
            .andDo(print());
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
                                     boolean ketogenicTag) {
        return Product.builder()
            .board(board)
            .title("title")
            .price(1000)
            .category(Category.BREAD)
            .glutenFreeTag(glutenFreeTag)
            .highProteinTag(highProteinTag)
            .sugarFreeTag(sugarFreeTag)
            .veganTag(veganTag)
            .ketogenicTag(ketogenicTag)
            .build();
    }


}
