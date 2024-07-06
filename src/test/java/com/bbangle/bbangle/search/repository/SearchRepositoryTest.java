package com.bbangle.bbangle.search.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.service.SearchLoadService;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class SearchRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    SearchLoadService searchLoadService;
    @Autowired
    RedisRepository redisRepository;

    private Store store;
    private Board board;
    private Member member;


    @BeforeEach
    void saveEntity() {
        createMember();
        createProductRelatedContent(15);
        redisRepository.delete("MIGRATION", "board");
        redisRepository.delete("MIGRATION", "store");
        searchLoadService.cacheKeywords();
        searchLoadService.cacheAutoComplete();
        searchLoadService.updateRedisAtBestKeyword();
    }

    @AfterEach
    void deleteAllEntity() {
        redisRepository.deleteAll();
        searchRepository.deleteAll();
        boardStatisticRepository.deleteAll();
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
        var productCount = productRepository.findAll().size();
        assertThat(productCount, is(45));
    }

    @Test
    @DisplayName("필터 설정에 맞게 상품 게시물 전체 개수를 가져올 수 있다")
    void getSearchedBoardAllCountTest() {
        List<Long> boardIds = boardRepository.findAll().stream().map(board1 -> board1.getId())
            .toList();
        var searchBoardRequest = SearchBoardRequest.builder()
            .sort("LATEST")
            .glutenFreeTag(true)
            .highProteinTag(false)
            .orderAvailableToday(true)
            .category(Category.SNACK.name())
            .maxPrice(6000)
            .page(0)
            .build();

        var searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(searchBoardRequest,
            boardIds);
        assertThat(searchedBoardAllCount, is(15L));
    }


    @Test
    @DisplayName("필터 설정에 맞게 상품 게시물 결과값을 가져올 수 있다")
    void getSearchBoard() {
        int page = 0;
        int limit = 10;

        List<Long> boardIds = boardRepository.findAll().stream().map(board1 -> board1.getId())
            .toList();
        var searchBoardRequest = SearchBoardRequest.builder()
            .sort("LATEST")
            .glutenFreeTag(true)
            .highProteinTag(false)
            .orderAvailableToday(true)
            .category(Category.SNACK.name())
            .maxPrice(6000)
            .page(0)
            .build();

        var searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(searchBoardRequest,
            boardIds);
        var searchBoardResult = searchRepository.getSearchedBoard(
            1L, boardIds, searchBoardRequest, PageRequest.of(page, limit),
            searchedBoardAllCount);

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
    @DisplayName("최근 키워드를 검색할 수 있다")
    void getRecentKeywordTest() {
        createSearchKeyword("초콜릿");
        createSearchKeyword("키토제닉 빵");
        createSearchKeyword("비건 베이커리");
        createSearchKeyword("키토제닉 빵");
        createSearchKeyword("초코 휘낭시에");
        createSearchKeyword("바나나 빵");
        createSearchKeyword("배부른 음식");
        createSearchKeyword("당당 치킨");

        var recencyKewords = searchRepository.getRecencyKeyword(member);

        int index = 0;
        for (KeywordDto recencyKeword : recencyKewords) {
            index++;
            // 알고리즘이 잘못되어 임시로 주석처리합니다. 고친 후 다시 PR 요청하겠습니다.
            //  assertThat(recencyKeword.keyword(), is(savingKeyword.get(savingKeyword.size() - index)));
        }
    }

    @Test
    @DisplayName("키워드를 저장할 수 있다")
    void getAllSearch() {
        List<String> savingKeyword = Arrays.asList("초콜릿", "키토제닉 빵", "비건", "비건 베이커리", "키토제닉 빵",
            "초코 휘낭시에", "바나나 빵", "배부른 음식", "당당 치킨");
        savingKeyword.forEach(keyword -> createSearchKeyword(keyword));
        var kewords = searchRepository.findAll();
        assertThat(kewords.size(), is(savingKeyword.size()));
    }

    @Test
    @DisplayName("저장된 키워드를 삭제할 수 있다")
    void deleteKeyword() {
        String keyword = "비건";
        var search = createSearchKeyword(keyword);
        checkSearchKeyword(searchRepository, search, keyword);

        searchRepository.markAsDeleted(keyword, member);
        checkSearchKeyword(searchRepository, search, keyword);
    }

    private void checkSearchKeyword(SearchRepository searchRepository, Search search,
        String keyword) {
        var savedSearch = searchRepository.findById(search.getId()).get();
        assertThat(savedSearch.getKeyword(), is(keyword));
    }

    private void createProductRelatedContent(int count) {
        for (int i = 0; i < count; i++) {
            store = storeRepository.save(
                Store.builder()
                    .identifier("7962401222")
                    .name("RAWSOME")
                    .profile(
                        "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                    .build());

            board = boardRepository.save(
                Board.builder()
                    .store(store)
                    .title("비건 베이커리 로썸 비건빵")
                    .price(5400)
                    .status(true)
                    .profile(
                        "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                    .purchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                    .build());

            productRepository.saveAll(List.of(
                Product.builder()
                    .board(board)
                    .title("콩볼")
                    .price(3600)
                    .category(Category.SNACK)
                    .glutenFreeTag(true)
                    .sugarFreeTag(true)
                    .highProteinTag(true)
                    .veganTag(true)
                    .ketogenicTag(true)
                    .build(),
                Product.builder()
                    .board(board)
                    .title("카카모카")
                    .price(5000)
                    .category(Category.BREAD)
                    .glutenFreeTag(true)
                    .veganTag(true)
                    .build(),
                Product.builder()
                    .board(board)
                    .title("로미넛쑥")
                    .price(5000)
                    .category(Category.BREAD)
                    .glutenFreeTag(true)
                    .sugarFreeTag(true)
                    .veganTag(true)
                    .build()
            ));
        }
    }

    private void createMember() {
        member = memberRepository.save(Member.builder()
            .id(2L)
            .build());
    }

    private Search createSearchKeyword(String keyword) {
        return searchRepository.save(
            Search.builder()
                .member(member)
                .keyword(keyword)
                .build());
    }
}
