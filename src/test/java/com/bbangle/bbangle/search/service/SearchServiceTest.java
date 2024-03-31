package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.util.KomoranUtil;
import com.bbangle.bbangle.util.TrieUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
@Transactional
@Rollback
public class SearchServiceTest {

    final String BOARD_NAMESPACE = RedisEnum.BOARD.name();
    final String SEARCH_KEYWORD = "비건 베이커리";
    final String SEARCH_KEYWORD_STORE = "RAWSOME";
    final String[] BOARD_IDS = {"1", "2", "3", "4", "5"};
    final int BOARD_PAGE = 0;
    final int STORE_PAGE = 0;
    private final String BEST_KEYWORD_KEY = "keyword";
    @Autowired
    SearchService searchService;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ProductRepository productRepository;
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

        Long memberId = 1L;
        var searchBoardRequest= SearchBoardRequest.builder()
                .keyword(SEARCH_KEYWORD)
                .sort("LATEST")
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(false)
                .ketogenicTag(false)
                .orderAvailableToday(true)
                .category("Cookie")
                .minPrice(0)
                .maxPrice(6000)
                .page(0)
                .build();


        var searchBoardResult = searchService.getSearchBoardDtos(
            memberId,searchBoardRequest);

        var boards = searchBoardResult.content();

        int currentItemCount = searchBoardResult.currentItemCount();
        int pageNumber = searchBoardResult.pageNumber();
        int itemAllCount = searchBoardResult.itemAllCount();

        Assertions.assertEquals(15, currentItemCount, "전체 아이템 개수가 다릅니다");

        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(boards.size() <= itemAllCount);

        Assertions.assertEquals(1L, boards.get(0)
            .boardId());
        Assertions.assertEquals(1L, boards.get(0)
            .storeId());
        Assertions.assertEquals("RAWSOME", boards.get(0)
            .storeName());
        Assertions.assertEquals(
            "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203"
            , boards.get(0)
                .thumbnail());
        Assertions.assertEquals("비건 베이커리 로썸 비건빵", boards.get(0)
            .title());
        Assertions.assertEquals(5400, boards.get(0)
            .price());
        Assertions.assertEquals(true, boards.get(0)
            .isWished());
        Assertions.assertEquals(List.of("glutenFree", "sugarFree", "vegan"), boards.get(0)
            .tags());

        Assertions.assertEquals(2L, boards.get(1)
            .boardId());
        Assertions.assertEquals(2L, boards.get(1)
            .storeId());
        Assertions.assertEquals("RAWSOME", boards.get(1)
            .storeName());
        Assertions.assertEquals(
            "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203"
            , boards.get(1)
                .thumbnail());
        Assertions.assertEquals("비건 베이커리 로썸 비건빵", boards.get(1)
            .title());
        Assertions.assertEquals(5400, boards.get(1)
            .price());
        Assertions.assertEquals(true, boards.get(1)
            .isWished());
        Assertions.assertEquals(List.of("glutenFree", "sugarFree", "vegan"), boards.get(1)
            .tags());
    }

    @Test
    @DisplayName("검색한 내용에 대한 게시판 결과값을 얻을 수 있다")
    public void getSearchStore() {
        Long memberId = 1L;
        String sort = "LATEST";
        Boolean glutenFreeTag = true;
        Boolean highProteinTag = false;
        Boolean sugarFreeTag = false;
        Boolean veganTag = false;
        Boolean ketogenicTag = false;
        String category = "COOKIE";
        Integer minPrice = 0;
        Integer maxPrice = 6000;

        var searchStoreResult = searchService.getSearchStoreDtos(
            memberId, STORE_PAGE, SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();
        int itemAllCount = searchStoreResult.itemAllCount();
        int pageNumber = searchStoreResult.pageNumber();
        int limitItemCount = searchStoreResult.limitItemCount();

        Assertions.assertEquals(15, itemAllCount, "전체 아이템 개수가 다릅니다");
        Assertions.assertTrue(stores.size() <= limitItemCount);
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertEquals(10, limitItemCount);

        Assertions.assertEquals(1L, stores.get(0)
            .storeId());
        Assertions.assertEquals("RAWSOME", stores.get(0)
            .storeName());
        Assertions.assertEquals(false, stores.get(0)
            .isWished());

        Assertions.assertEquals(2L, stores.get(1)
            .storeId());
        Assertions.assertEquals("RAWSOME", stores.get(1)
            .storeName());
        Assertions.assertEquals(false, stores.get(1)
            .isWished());


        searchStoreResult = searchService.getSearchStoreDtos(
            memberId,STORE_PAGE + 1, SEARCH_KEYWORD_STORE);


        stores = searchStoreResult.content();
        itemAllCount = searchStoreResult.itemAllCount();
        pageNumber = searchStoreResult.pageNumber();
        limitItemCount = searchStoreResult.limitItemCount();

        Assertions.assertEquals(15, itemAllCount, "전체 아이템 개수가 다릅니다");
        Assertions.assertEquals(5, stores.size());
        Assertions.assertEquals(1, pageNumber);
        Assertions.assertEquals(10, limitItemCount);

        Assertions.assertEquals(11L, stores.get(0)
            .storeId());
        Assertions.assertEquals("RAWSOME", stores.get(0)
            .storeName());
        Assertions.assertEquals(false, stores.get(0)
            .isWished());

        Assertions.assertEquals(12L, stores.get(1)
            .storeId());
        Assertions.assertEquals("RAWSOME", stores.get(1)
            .storeName());
        Assertions.assertEquals(false, stores.get(1)
            .isWished());
    }

    @Test
    @DisplayName("검색한 내용에 대한 게시판 결과값을 얻을 수 있다")
    public void getSearchStoreTest() {
        Long memberId = 1L;

        var searchStoreResult = searchService.getSearchStoreDtos(
            memberId,STORE_PAGE + 1, SEARCH_KEYWORD_STORE);

        var stores = searchStoreResult.content();
        var itemAllCount = searchStoreResult.itemAllCount();
        var pageNumber = searchStoreResult.pageNumber();
        var limitItemCount = searchStoreResult.limitItemCount();

        Assertions.assertEquals(15, itemAllCount, "전체 아이템 개수가 다릅니다");
        Assertions.assertEquals(5, stores.size());
        Assertions.assertEquals(1, pageNumber);
        Assertions.assertEquals(10, limitItemCount);

        Assertions.assertEquals(11L, stores.get(0).storeId());
        Assertions.assertEquals("RAWSOME", stores.get(0).storeName());
        Assertions.assertEquals(false, stores.get(0).isWished());

        Assertions.assertEquals(12L, stores.get(1).storeId());
        Assertions.assertEquals("RAWSOME", stores.get(1).storeName());
        Assertions.assertEquals(false, stores.get(1).isWished());
    }

    @Test
    public void getAllBoardTitleTest() {

        var result = boardRepository.getAllBoardTitle();
    }


    private List<String> komoranUtil(String title) {
        var komoran = KomoranUtil.getInstance();
        KomoranResult analyzeResultList = komoran.analyze(title);

        return analyzeResultList.getMorphesByTags("NNG", "NNP", "NNB", "SL");
    }

    @Test
    public void getBestKeyword() {
        var result = redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );

        Assertions.assertTrue(result.size() < 7);

        searchService.updateRedisAtBestKeyword();

        result = redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );

        Assertions.assertTrue(result.size() < 7);
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
