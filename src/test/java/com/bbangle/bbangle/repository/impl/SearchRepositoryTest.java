package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.dto.KeywordDto;
import com.bbangle.bbangle.model.*;
import com.bbangle.bbangle.repository.*;
import com.bbangle.bbangle.service.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Transactional
@Rollback
public class SearchRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SearchRepository searchRepository;

    @Autowired
    SearchService searchService;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;


    final List<Long> BOARD_IDS = Arrays.asList(1L,2L,3L,4L,5L);
    final List<Long> STORE_IDS = Arrays.asList(1L,2L);

    private final Long DEFAULT_MEMBER_ID = 2L;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void saveData(){
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

        this.entityManager
                .createNativeQuery("ALTER TABLE search ALTER COLUMN `id` RESTART WITH 1")
                .executeUpdate();

        this.entityManager
                .createNativeQuery("ALTER TABLE member ALTER COLUMN `id` RESTART WITH 2")
                .executeUpdate();
    }


    @Test
    public void getSearchBoard(){
        //스토어 및 보드 검색 결과 가져오기
        var searchBoardResult = searchRepository.getSearchResult(BOARD_IDS);

        // 각 BoardResponseDto에 대한 처리
        Assertions.assertEquals(1L, searchBoardResult.get(0).boardId());
        Assertions.assertEquals(1L, searchBoardResult.get(0).storeId());
        Assertions.assertEquals("RAWSOME", searchBoardResult.get(0).storeName());
        Assertions.assertEquals("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203"
                , searchBoardResult.get(0).thumbnail());
        Assertions.assertEquals("비건 베이커리 로썸 비건빵", searchBoardResult.get(0).title());
        Assertions.assertEquals(5400, searchBoardResult.get(0).price());
        Assertions.assertEquals(true, searchBoardResult.get(0).isWished());
        Assertions.assertEquals(List.of("glutenFree","sugarFree","vegan"), searchBoardResult.get(0).tags());

        Assertions.assertEquals(2L, searchBoardResult.get(1).boardId());
        Assertions.assertEquals(2L, searchBoardResult.get(1).storeId());
        Assertions.assertEquals("RAWSOME", searchBoardResult.get(1).storeName());
        Assertions.assertEquals("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203"
                , searchBoardResult.get(1).thumbnail());
        Assertions.assertEquals("비건 베이커리 로썸 비건빵", searchBoardResult.get(1).title());
        Assertions.assertEquals(5400, searchBoardResult.get(1).price());
        Assertions.assertEquals(true, searchBoardResult.get(1).isWished());
        Assertions.assertEquals(List.of("glutenFree","sugarFree","vegan"), searchBoardResult.get(1).tags());
    }

    @Test
    public void getSearchStore(){
        //스토어 및 보드 검색 결과 가져오기
        var searchStoreResult = searchRepository.getSearchedStore(STORE_IDS);

        // 각 BoardResponseDto에 대한 처리
        Assertions.assertEquals(1L, searchStoreResult.get(0).storeId());
        Assertions.assertEquals("RAWSOME", searchStoreResult.get(0).storeName());
        Assertions.assertEquals(false, searchStoreResult.get(0).isWished());

        Assertions.assertEquals(2L, searchStoreResult.get(1).storeId());
        Assertions.assertEquals("RAWSOME", searchStoreResult.get(1).storeName());
        Assertions.assertEquals(false, searchStoreResult.get(1).isWished());
    }

    @Test
    public void getBestKeywordTest(){
        List<String> savingKeyword = List.of("키토제닉 빵","비건", "비건 베이커리", "키토제닉 빵", "초코 휘낭시에");

        var member = createMemberData();
        var savedKeywords = createSearchData(
                savingKeyword, member
        );

        // 값이 잘 들어갔는지 확인
        int index=0;
        Assertions.assertEquals(savingKeyword.size(), savedKeywords.size(), "저장된 개수가 다릅니다");
        for (Search savedKeyword : savedKeywords){
            Assertions.assertEquals(DEFAULT_MEMBER_ID, savedKeyword.getMember().getId(), "저장된 아이디 값이 다릅니다");
            Assertions.assertEquals(savingKeyword.get(index++), savedKeyword.getKeyword(), "저장된 값이 다릅니다");
        }

        // 베스트 키워드 값 확인
        List<String> expectList = List.of("키토제닉 빵","비건", "비건 베이커리", "초코 휘낭시에");

        index=0;
        String[] keywords = searchRepository.getBestKeyword();
        Assertions.assertEquals(expectList.size(), keywords.length, "베스트 검색어 개수가 다릅니다");

        // 많이 나오는 순 -> 글자 오름차순 정렬
        for (String keyword:keywords){
            Assertions.assertEquals(expectList.get(index++), keyword, "저장된 값이 다릅니다");
        }
    }

    @Test
    public void getRecentKeywordTest() {
        List<String> savingKeyword = Arrays.asList("초콜릿","키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵", "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");

        var member = createMemberData();
        var savedKeywords = createSearchData(
                savingKeyword, member
        );

        // 값이 잘 들어갔는지 확인
        int index=0;
        Assertions.assertEquals(savingKeyword.size(), savedKeywords.size(), "저장된 개수가 다릅니다");
        for (Search savedKeyword : savedKeywords){
            Assertions.assertEquals(DEFAULT_MEMBER_ID, savedKeyword.getMember().getId(), "저장된 값이 다릅니다");
            Assertions.assertEquals(savingKeyword.get(index++), savedKeyword.getKeyword(), "저장된 값이 다릅니다");
        }

        var result = searchRepository.getRecencyKeyword(member);

        index=0;
        List<String> expectList = List.of("비건", "비건 베이커리","키토제닉 빵", "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
        Assertions.assertEquals(expectList.size(), result.size(), "베스트 검색어 개수가 다릅니다");

        // 많이 나오는 순 -> 글자 오름차순 정렬
        for (KeywordDto keyword:result){
            System.out.println(String.format("%s, %s", expectList.get(expectList.size() - 1 - index), result));
            Assertions.assertEquals(expectList.get(expectList.size() - ++index), keyword.keyword(), "저장된 값이 다릅니다");
        }
    }

    @Test
    public void getDeleteKeywordTest() {
        List<String> savingKeyword = Arrays.asList("초콜릿","키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵", "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");

        var member = createMemberData();
        var savedKeywords = createSearchData(
                savingKeyword, member
        );

        // 값이 잘 들어갔는지 확인
        int index=0;
        Assertions.assertEquals(savingKeyword.size(), savedKeywords.size(), "저장된 개수가 다릅니다");
        for (Search savedKeyword : savedKeywords){
            Assertions.assertEquals(DEFAULT_MEMBER_ID, savedKeyword.getMember().getId(), "저장된 값이 다릅니다");
            Assertions.assertEquals(savingKeyword.get(index++), savedKeyword.getKeyword(), "저장된 값이 다릅니다");
        }

        searchRepository.markAsDeleted(savingKeyword.get(1), member);

        var result = searchRepository.getRecencyKeyword(member);

        var a = searchRepository.findAll();

        index=0;
        List<String> expectList = List.of("초콜릿", "비건", "비건 베이커리","초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
        Assertions.assertEquals(expectList.size(), result.size(), "베스트 검색어 개수가 다릅니다");

        // 많이 나오는 순 -> 글자 오름차순 정렬
        for (KeywordDto keyword:result){
            Assertions.assertEquals(expectList.get(expectList.size() - ++index), keyword.keyword(), "저장된 값이 다릅니다");
        }
    }

    private Member createMemberData(){
        return memberRepository.save(
                Member.builder()
                        .build());
    }


    private List<Search> createSearchData(List<String> keywords, Member member){
        AtomicInteger index = new AtomicInteger(1);
        searchRepository.saveAll(
                keywords.stream().map(s ->
                        Search.builder()
                                .member(member)
                                .keyword(s)
                                .createdAt(LocalDateTime.now().plusSeconds(index.getAndIncrement()))
                                .build()
                ).toList()
        );

         return searchRepository.findAll();
    }

    private void createData(int count){
        for(int i=0; i<count; i++){
            var store = Store.builder()
                    .identifier("7962401222")
                    .name("RAWSOME")
                    .profile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                    .introduce("건강을 먹다-로썸")
                    .build();

            var board = Board.builder()
                    .store(store)
                    .title("비건 베이커리 로썸 비건빵")
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
    }
}
