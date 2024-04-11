package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.testutil.TestFactoryManager;
import com.bbangle.bbangle.testutil.model.*;
import com.bbangle.bbangle.util.TrieUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@Rollback
public class SearchServiceTest {
    private final TestFactoryManager testFactoryManager;

    @Autowired
    RedisRepository redisRepository;
    @Autowired
    SearchService searchService;

    public SearchServiceTest(
            @Autowired MemberRepository memberRepository,
            @Autowired SearchRepository searchRepository,
            @Autowired StoreRepository storeRepository,
            @Autowired BoardRepository boardRepository,
            @Autowired ProductRepository productRepository,
            @Autowired EntityManager entityManager
    ){
        testFactoryManager = new TestFactoryManager(entityManager)
                .setTestMemberFactory(memberRepository)
                .setTestSearchFactory(searchRepository)
                .setTestStoreFactory(storeRepository)
                .setTestBoardFactory(boardRepository)
                .setTestProductFactory(productRepository);
    }

    @BeforeEach
    public void saveData() {
        createMember();
        createProductRelatedContent(15);
    }

    @AfterEach
    void afterEach() {
        testFactoryManager.resetAutoIncreasementAndRowData();
    }

    @Test
    @DisplayName("게시물이 잘 저장돼있다")
    public void checkAllBoardCountTest(){
        var boardCount = testFactoryManager.getTestBoardFactory().getRepository().findAll().size();
        assertThat(boardCount, is(15));
    }

    @Test
    @DisplayName("자동완성 알고리즘에 값을 저장하면 정상적으로 저장한 값을 불러올 수 있다")
    public void trieUtilTest() {
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
    public void getSearchBoard() {
        String SEARCH_KEYWORD = "비건 베이커리";
        Member member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        var searchBoardRequest= SearchBoardRequest.builder()
                .keyword(SEARCH_KEYWORD)
                .sort("LATEST")
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(false)
                .ketogenicTag(false)
                .orderAvailableToday(true)
                .category(Category.COOKIE.name())
                .minPrice(0)
                .maxPrice(6000)
                .page(0)
                .build();

        var searchBoardResult = searchService.getSearchBoardDtos(
                member.getId(),
                searchBoardRequest);

        assertThat(searchBoardResult.currentItemCount(), is(10));
        assertThat(searchBoardResult.pageNumber(), is(0));
        assertThat(searchBoardResult.itemAllCount(), is(15L));
        assertThat(searchBoardResult.limitItemCount(), is(10));
        assertThat(searchBoardResult.existNextPage(), is(true));

        var BoardDtos = searchBoardResult.content();
        for(int i = 0; BoardDtos.size() > i; i++){
            var boardDto = BoardDtos.get(i);
            assertThat(boardDto.boardId(), is(i+1L));
            assertThat(boardDto.storeId(), is(i+1L));
            assertThat(boardDto.tags(), hasItem("glutenFree"));
            assertThat(boardDto.price(), lessThanOrEqualTo(6000));
        }
    }

    @Test
    @DisplayName("검색을 통해 스토어를 찾을 수 있다")
    public void getSearchedStore() {
        int storePage = 0;
        String SEARCH_KEYWORD_STORE = "RAWSOME";
        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        var searchStoreResult = searchService.getSearchStoreDtos(
                member.getId(), storePage, SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();

        assertThat(searchStoreResult.currentItemCount(), is(10));
        assertThat(searchStoreResult.pageNumber(), is(0));
        assertThat(searchStoreResult.itemAllCount(), is(15));
        assertThat(searchStoreResult.limitItemCount(), is(10));
        assertThat(searchStoreResult.existNextPage(), is(true));

        for (int i = 0; stores.size() > i; i++){
            var store = stores.get(i);
            assertThat(store.storeId(), is(i+1L));
            assertThat(store.storeName(), is("RAWSOME"));
            assertThat(store.isWished(), is(false));


        }
    }

    @Test
    @DisplayName("검색된 스토어 데이터를 무한스크롤로 구현할 수 있다")
    public void TestInfiniteScroll() {
        int storePage = 1;
        String SEARCH_KEYWORD_STORE = "RAWSOME";
        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        var searchStoreResult = searchService.getSearchStoreDtos(
                member.getId(), storePage, SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();

        assertThat(searchStoreResult.currentItemCount(), is(5));
        assertThat(searchStoreResult.pageNumber(), is(storePage));
        assertThat(searchStoreResult.itemAllCount(), is(15));
        assertThat(searchStoreResult.limitItemCount(), is(10));
        assertThat(searchStoreResult.existNextPage(), is(false));

        for (int i = 0; stores.size() > i; i++){
            var store = stores.get(i);
            assertThat(store.storeId(), is(i+11L));
            assertThat(store.storeName(), is("RAWSOME"));
            assertThat(store.isWished(), is(false));
        }
    }

    @Test
    @DisplayName("기본으로 등록된 베스트 키워드를 가져올 수 있다")
    public void getBestKeyword() {
        String BEST_KEYWORD_KEY = "keyword";
        var bestKewords = redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );

        assertThat(bestKewords, is(List.of("글루텐프리", "비건", "저당", "키토제닉")));
    }

    private void createProductRelatedContent(int count) {
        for (int i = 0; i < count; i++) {
            var store = testFactoryManager.getTestStoreFactory().pushTestEntity(
                    "store" + i,
                    new TestStore().setIdentifier("7962401222")
                            .setName("RAWSOME")
                            .setProfile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                            .getModel()
            );

            var board = testFactoryManager.getTestBoardFactory().pushTestEntity(
                    "board" + i,
                    new TestBoard(store)
                            .setBoardName("비건 베이커리 로썸 비건빵")
                            .setPrice(5400)
                            .setStatus(true)
                            .setProfile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                            .setPurchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                            .setView(100)
                            .setSunday(true)
                            .setMonday(true)
                            .setTuesday(true)
                            .setWednessday(true)
                            .setThursday(true)
                            .setFriday(true)
                            .setSaturday(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3 - 2),
                    new TestProduct(board)
                            .setProductName("콩볼")
                            .setPrice(3600)
                            .setCategory(Category.COOKIE)
                            .setGlutenFreeTag(true)
                            .setSugarFreeTag(true)
                            .setVeganTag(true)
                            .setKetogenicTag(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3 - 1),
                    new TestProduct(board)
                            .setProductName("카카모카")
                            .setPrice(5000)
                            .setCategory(Category.BREAD)
                            .setGlutenFreeTag(true)
                            .setVeganTag(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3),
                    new TestProduct(board)
                            .setProductName("로미넛쑥")
                            .setPrice(5000)
                            .setCategory(Category.BREAD)
                            .setGlutenFreeTag(true)
                            .setSugarFreeTag(true)
                            .setVeganTag(true)
                            .getModel()
            );
        }
    }
    private void createMember() {
        testFactoryManager.getTestMemberFactory().pushTestEntity(
                "member1",
                new TestMember().getModel()
        );
    }

    private Search createSearchKeyword(String keyword) {
        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        return testFactoryManager.getTestSearchFactory().pushTestEntity(
                "keyword_" + keyword,
                new TestSearch()
                        .setMember(member)
                        .setKeyword(keyword)
                        .getModel()
        );
    }

}
