package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.configuration.AbstractRestDocsTests;
import com.bbangle.bbangle.model.Board;
import com.bbangle.bbangle.model.Category;
import com.bbangle.bbangle.model.Product;
import com.bbangle.bbangle.model.Store;
import com.bbangle.bbangle.repository.BoardRepository;
import com.bbangle.bbangle.repository.ProductRepository;
import com.bbangle.bbangle.repository.StoreRepository;
import com.bbangle.bbangle.service.SearchService;
import jakarta.transaction.Transactional;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchControllerTest extends AbstractRestDocsTests {
    /*
     * 우테코 기술 블로그
     * https://techblog.woowahan.com/2597/
     *
     * Spring Rest Docs 예제 참고용
     * https://velog.io/@chaerim1001/Spring-Rest-Docs-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-AsciiDoc-%EB%AC%B8%EB%B2%95
     * */

    @MockBean
    private SearchService searchService;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    public void saveData(){
        var store = Store.builder()
                .id(1L)
                .identifier("7962401222")
                .name("RAWSOME")
                .profile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                .introduce("건강을 먹다-로썸")
                .build();

        var board = Board.builder()
                .id(1L)
                .store(store)
                .title("비건베이커리 로썸 비건빵")
                .price(5400)
                .status(true)
                .profile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                .detail("test.txt")
                .purchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                .view(100)
                .sunday(false).monday(false).tuesday(false).wednesday(false).thursday(true).sunday(false)
                .build();

        var product1 = Product.builder()
                .board(board)
                .title("콩볼")
                .price(3600)
                .category(Category.COOKIE)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(true)
                .veganTag(true)
                .ketogenicTag(false)
                .build();

        var product2 = Product.builder()
                .board(board)
                .title("카카모카")
                .price(5000)
                .category(Category.BREAD)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(true)
                .ketogenicTag(false)
                .build();

        var product3 = Product.builder()
                .board(board)
                .title("로미넛쑥")
                .price(5000)
                .category(Category.BREAD)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(true)
                .ketogenicTag(false)
                .build();

        storeRepository.save(store);
        boardRepository.save(board);
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    @AfterEach
    @Transactional
    public void deleteData(){
        productRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();
    }

    @Test
    public void getSearchedBoard() throws Exception {
        String keyword = "비건";

        List<Store> storeList =  storeRepository.findAll();

        storeList.forEach(store -> {
            Assertions.assertEquals(store.getName(), "RAWSOME");
        });

        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/search")
                        .param("keyword",keyword)
                        .with(SecurityMockMvcRequestPostProcessors.user("user").password("password").roles("USER")));
        resultActions.andExpect(status().isOk()).andDo(
                document("search-controller-test/get-searched-board")
        );
    }
}
