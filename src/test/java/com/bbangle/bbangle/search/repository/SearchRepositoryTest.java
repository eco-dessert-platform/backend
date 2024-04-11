package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.testutil.TestFactoryManager;
import com.bbangle.bbangle.testutil.model.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@Transactional
@Rollback
public class SearchRepositoryTest {
    TestFactoryManager testFactoryManager;

    public SearchRepositoryTest(
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
    @DisplayName("필터 설정에 맞게 상품 게시물 전체 개수를 가져올 수 있다")
    public void getSearchedBoardAllCountTest(){
        List<Long> boardIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L);
        var searchBoardRequest= SearchBoardRequest.builder()
                .sort("LATEST")
                .glutenFreeTag(true)
                .highProteinTag(false)
                .orderAvailableToday(true)
                .category(Category.COOKIE.name())
                .maxPrice(6000)
                .page(0)
                .build();

        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        var searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(searchBoardRequest, boardIds);
        assertThat(searchedBoardAllCount, is(12L));
    }


    @Test
    @DisplayName("필터 설정에 맞게 상품 게시물 결과값을 가져올 수 있다")
    public void getSearchBoard() {
        int page = 0;
        int limit = 10;

        List<Long> boardIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L);
        var searchBoardRequest= SearchBoardRequest.builder()
                .sort("LATEST")
                .glutenFreeTag(true)
                .highProteinTag(false)
                .orderAvailableToday(true)
                .category(Category.COOKIE.name())
                .maxPrice(6000)
                .page(0)
                .build();

        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        var searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(searchBoardRequest, boardIds);
        var searchBoardResult = searchRepository.getSearchedBoard(
                1L, boardIds, searchBoardRequest, PageRequest.of(page, limit), searchedBoardAllCount);

        assertThat(searchBoardResult.currentItemCount(), is(10));
        assertThat(searchBoardResult.pageNumber(), is(0));
        assertThat(searchBoardResult.itemAllCount(), is(12L));
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
    @DisplayName("스토어 검색")
    public void getSearchStoreTest(){
        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        List<Long> storeIds = Arrays.asList(1L, 2L);
        var searchedStores = searchRepository.getSearchedStore(member.getId(), storeIds, PageRequest.of(0, 10));

        for(int i = 0; i < 2; i++){
            assertThat(searchedStores.get(i).storeId(), is(i + 1));
            assertThat(searchedStores.get(i).storeName(), is("RAWSOME"));
            assertThat(searchedStores.get(i).isWished(), is(false));
        }
    }

    @Test
    @DisplayName("최근 키워드를 검색할 수 있다")
    public void getRecentKeywordTest() {
        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        List<String> savingKeyword = Arrays.asList("초콜릿", "키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵",
            "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
        savingKeyword.forEach(keyword-> createSearchKeyword(keyword));

        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        var recencyKewords = searchRepository.getRecencyKeyword(member);

        int index = 0;
        for(KeywordDto recencyKeword : recencyKewords){
            index ++;
            assertThat(recencyKeword.keyword(), is(savingKeyword.get(savingKeyword.size() - index)));
        }
    }
    @Test
    @DisplayName("키워드를 저장할 수 있다")
    public void getAllSearch(){
        List<String> savingKeyword = Arrays.asList("초콜릿", "키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵",
                "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        savingKeyword.forEach(keyword-> createSearchKeyword(keyword));
        var kewords = searchRepository.findAll();
        assertThat( kewords.size(),is(savingKeyword.size()));
    }

    @Test
    @DisplayName("저장된 키워드를 삭제할 수 있다")
    public void deleteKeyword(){
        String keyword = "비건";
        var searchRepository = testFactoryManager.getTestSearchFactory().getRepository();
        var search = createSearchKeyword(keyword);
        checkSearchKeyword(searchRepository, search, keyword);

        var member = testFactoryManager.getTestMemberFactory().getTestEntity("member1");
        searchRepository.markAsDeleted(keyword, member);
        checkSearchKeyword(searchRepository, search, keyword);
    }

    private void checkSearchKeyword(SearchRepository searchRepository, Search search, String keyword){
        var savedSearch = searchRepository.findById(search.getId()).get();
        assertThat(savedSearch.getKeyword(), is(keyword));
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
