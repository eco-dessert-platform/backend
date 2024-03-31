package com.bbangle.bbangle.search.controller;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.configuration.AbstractRestDocsTests;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.search.service.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class SearchControllerTest extends AbstractRestDocsTests {
    /*
     * 우테코 기술 블로그
     * https://techblog.woowahan.com/2597/
     *
     * Spring Rest Docs 예제 참고용
     * https://velog.io/@chaerim1001/Spring-Rest-Docs-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-AsciiDoc-%EB%AC%B8%EB%B2%95
     * */

    private final String BEARER = "Bearer";
    private final String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYmFuZ2xlYmJhbmdsZSIsImlhdCI6MTcwNzMxNDI4NCwiZXhwIjoxNzA3MzI1MDg0LCJpZCI6NX0.eantDIlCpNQpjA2aIYKgfp6yToFg8C-W3z355cR-Wio";
    @Autowired
    private SearchService searchService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    private SearchRepository searchRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void saveData() {
        createData(15);
        redisRepository.deleteAll();
        searchService.initSetting();
    }

    @AfterEach
    void afterEach() {
        this.entityManager
            .createNativeQuery("ALTER TABLE store ALTER COLUMN `id` RESTART WITH 1")
            .executeUpdate();

        this.entityManager
            .createNativeQuery("ALTER TABLE product_board ALTER COLUMN `id` RESTART WITH 1")
            .executeUpdate();
    }

    @Test
    public void getSearchedBoard() throws Exception {
        String keyword = "비건 베이커리";
        String storePage = "0";
        String boardPage = "0";

        mockMvc.perform(
                get("/api/v1/search")
                    .param("keyword", keyword)
                    .param("storePage", storePage)
                    .param("boardPage", boardPage)
                    .with(SecurityMockMvcRequestPostProcessors.user("user")
                        .password("password")
                        .roles("USER")))
            .andExpect(status().isOk());
    }

//    @Test
//    public void getRecentKeyword() throws Exception {
//        List<String> savingKeyword = Arrays.asList("초콜릿","키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵", "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
//
//        var member = createMemberData();
//        var savedKeywords = createSearchData(
//                savingKeyword, member
//        );
//
//        mockMvc.perform(
//                get("/api/v1/search/recency")
//                        .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION)))
//                        .andExpect(status().isOk());
//    }

    private Member createMemberData() {
        return memberRepository.save(
            Member.builder()
                .build());
    }


    private List<Search> createSearchData(List<String> keywords, Member member) {
        AtomicInteger index = new AtomicInteger(1);
        searchRepository.saveAll(
            keywords.stream()
                .map(s ->
                    Search.builder()
                        .member(member)
                        .keyword(s)
                        .createdAt(LocalDateTime.now()
                            .plusSeconds(index.getAndIncrement()))
                        .build()
                )
                .toList()
        );

        return searchRepository.findAll();
    }

    private void createData(int count) {
        for (int i = 0; i < count; i++) {
            var store = Store.builder()
                .identifier("7962401222")
                .name("RAWSOME")
                .profile(
                    "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                .introduce("건강을 먹다-로썸")
                .build();

            var board = Board.builder()
                .store(store)
                .title("비건 베이커리 로썸 비건빵")
                .price(5400)
                .status(true)
                .profile(
                    "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                .detail("test.txt")
                .purchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                .view(100)
                .sunday(false)
                .monday(false)
                .tuesday(false)
                .wednesday(false)
                .thursday(true)
                .sunday(false)
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
    }

}
