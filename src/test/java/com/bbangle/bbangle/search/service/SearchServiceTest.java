package com.bbangle.bbangle.search.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.util.TrieUtil;
import jakarta.persistence.EntityManager;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SearchServiceTest extends AbstractIntegrationTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    SearchService searchService;
    @Autowired
    RankingRepository rankingRepository;
    @Autowired
    EntityManager entityManager;

    private Store store;
    private Board board;
    private Member member;


    @BeforeEach
    void saveEntity() {
        createMember();
        createProductRelatedContent(15);
        redisRepository.delete("MIGRATION", "board");
        redisRepository.delete("MIGRATION", "store");
        searchService.initSetting();
        searchService.updateRedisAtBestKeyword();
    }

    @AfterEach
    void deleteAllEntity() {
        redisRepository.deleteAll();
        searchRepository.deleteAll();
        rankingRepository.deleteAll();
        memberRepository.deleteAll();
        productRepository.deleteAll();
        boardRepository.deleteAll();
        storeRepository.deleteAll();
    }

    @Test
    @DisplayName("게시물이 잘 저장돼있다")
    void checkAllBoardCountTest() {
        var boardCount = boardRepository.findAll().size();
        assertThat(boardCount, is(15));
    }

    @Test
    @DisplayName("자동완성 알고리즘에 값을 저장하면 정상적으로 저장한 값을 불러올 수 있다")
    void trieUtilTest() {
        TrieUtil trieUtil = new TrieUtil();

        trieUtil.insert("비건 베이커리");
        trieUtil.insert("비건");
        trieUtil.insert("비건 베이커리 짱짱");
        trieUtil.insert("초코송이");

        var resultOne = trieUtil.autoComplete("초", 1);
        Assertions.assertEquals(resultOne, List.of("초코송이"));
        Assertions.assertEquals(resultOne.size(), 1);

        var resultTwo = trieUtil.autoComplete("비", 2);
        Assertions.assertEquals(resultTwo, List.of("비건", "비건 베이커리"));
        Assertions.assertEquals(resultTwo.size(), 2);

        var resultThree = trieUtil.autoComplete("비", 3);
        Assertions.assertEquals(resultThree, List.of("비건", "비건 베이커리", "비건 베이커리 짱짱"));
        Assertions.assertEquals(resultThree.size(), 3);

        var resultFour = trieUtil.autoComplete("바", 3);
        Assertions.assertEquals(resultFour, List.of());
        Assertions.assertEquals(resultFour.size(), 0);
    }

    @Test
    @DisplayName("검색한 내용에 대한 게시판 결과값을 얻을 수 있다")
    void getSearchBoard() {
        String SEARCH_KEYWORD = "비건 베이커리";
        var searchBoardRequest = SearchBoardRequest.builder().keyword(SEARCH_KEYWORD).sort("LATEST")
                .glutenFreeTag(true).highProteinTag(false).sugarFreeTag(false).veganTag(false)
                .ketogenicTag(false).orderAvailableToday(true).category(Category.SNACK.name())
                .minPrice(0).maxPrice(6000).page(0).build();

        var searchBoardResult = searchService.getSearchBoardDtos(member.getId(),
                searchBoardRequest);

        assertThat(searchBoardResult.currentItemCount(), is(10));
        assertThat(searchBoardResult.pageNumber(), is(0));
        assertThat(searchBoardResult.itemAllCount(), is(15L));
        assertThat(searchBoardResult.limitItemCount(), is(10));
        assertThat(searchBoardResult.existNextPage(), is(true));

        var BoardDtos = searchBoardResult.content();
        for (int i = 0; BoardDtos.size() > i; i++) {
            var boardDto = BoardDtos.get(i);
            assertThat(boardDto.getTags(), hasItem("glutenFree"));
            assertThat(boardDto.getPrice(), lessThanOrEqualTo(6000));
        }
    }

    @Test
    @DisplayName("검색을 통해 스토어를 찾을 수 있다")
    void getSearchedStore() {
        int storePage = 0;
        String SEARCH_KEYWORD_STORE = "RAWSOME";
        var searchStoreResult = searchService.getSearchStoreDtos(member.getId(), storePage,
                SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();

        assertThat(searchStoreResult.currentItemCount(), is(10));
        assertThat(searchStoreResult.pageNumber(), is(0));
        assertThat(searchStoreResult.itemAllCount(), is(15));
        assertThat(searchStoreResult.limitItemCount(), is(10));
        assertThat(searchStoreResult.existNextPage(), is(true));

        for (int i = 0; stores.size() > i; i++) {
            var store = stores.get(i);
            assertThat(store.getStoreName(), is("RAWSOME"));
            assertThat(store.getIsWished(), is(false));


        }
    }

    @Test
    @DisplayName("검색된 스토어 데이터를 무한스크롤로 구현할 수 있다")
    void TestInfiniteScroll() {
        int storePage = 1;
        String SEARCH_KEYWORD_STORE = "RAWSOME";
        var searchStoreResult = searchService.getSearchStoreDtos(member.getId(), storePage,
                SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();

        assertThat(searchStoreResult.currentItemCount(), is(5));
        assertThat(searchStoreResult.pageNumber(), is(storePage));
        assertThat(searchStoreResult.itemAllCount(), is(15));
        assertThat(searchStoreResult.limitItemCount(), is(10));
        assertThat(searchStoreResult.existNextPage(), is(false));

        for (int i = 0; stores.size() > i; i++) {
            var store = stores.get(i);
            assertThat(store.getStoreName(), is("RAWSOME"));
            assertThat(store.getIsWished(), is(false));
        }
    }

    @Test
    @DisplayName("기본으로 등록된 베스트 키워드를 가져올 수 있다")
    void getBestKeyword() {
        String BEST_KEYWORD_KEY = "keyword";
        var bestKewords = redisRepository.getStringList(RedisEnum.BEST_KEYWORD.name(),
                BEST_KEYWORD_KEY);

        assertThat(bestKewords, is(List.of("글루텐프리", "비건", "저당", "키토제닉")));
    }

    private void createProductRelatedContent(int count) {
        for (int i = 0; i < count; i++) {
            store = storeRepository.save(Store.builder().identifier("7962401222").name("RAWSOME")
                    .profile(
                            "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                    .build());

            board = boardRepository.save(
                    Board.builder().store(store).title("비건 베이커리 로썸 비건빵").price(5400).status(true)
                            .profile(
                                    "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                            .purchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                            .view(100).sunday(true).monday(true).tuesday(true).wednesday(true)
                            .thursday(true).friday(true).saturday(true).build());

            productRepository.saveAll(List.of(Product.builder().board(board).title("콩볼").price(3600)
                            .category(Category.SNACK).glutenFreeTag(true).sugarFreeTag(true).veganTag(true)
                            .ketogenicTag(true).build(),
                    Product.builder().board(board).title("카카모카").price(5000)
                            .category(Category.BREAD).glutenFreeTag(true).veganTag(true).build(),
                    Product.builder().board(board).title("로미넛쑥").price(5000)
                            .category(Category.BREAD).glutenFreeTag(true).sugarFreeTag(true)
                            .veganTag(true).build()));
        }
    }

    private void createMember() {
        member = memberRepository.save(Member.builder().id(2L).build());
    }

    private Search createSearchKeyword(String keyword) {
        return searchRepository.save(Search.builder().member(member).keyword(keyword).build());
    }
}
