package com.bbangle.bbangle.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SearchServiceTest extends AbstractIntegrationTest {

    @Autowired
    SearchRepository searchRepository;
    @Autowired
    SearchLoadService searchLoadService;
    @Autowired
    SearchService searchService;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    BoardStatisticService boardStatisticService;
    @Autowired
    AutoCompleteUtil autoCompleteUtil;


    @Test
    @DisplayName("자동완성 알고리즘에 값을 저장하면 정상적으로 저장한 값을 불러올 수 있다")
    void trieUtilTest() {
        autoCompleteUtil.insert("비건 베이커리");
        autoCompleteUtil.insert("비건");
        autoCompleteUtil.insert("비건 베이커리 짱짱");
        autoCompleteUtil.insert("초코송이");

        var resultOne = autoCompleteUtil.autoComplete("초", 1);
        Assertions.assertEquals(resultOne, List.of("초코송이"));
        Assertions.assertEquals(resultOne.size(), 1);

        var resultTwo = autoCompleteUtil.autoComplete("비", 2);
        Assertions.assertEquals(resultTwo, List.of("비건", "비건 베이커리"));
        Assertions.assertEquals(resultTwo.size(), 2);

        var resultThree = autoCompleteUtil.autoComplete("비", 3);
        Assertions.assertEquals(resultThree, List.of("비건", "비건 베이커리", "비건 베이커리 짱짱"));
        Assertions.assertEquals(resultThree.size(), 3);

        var resultFour = autoCompleteUtil.autoComplete("바", 3);
        Assertions.assertEquals(resultFour, List.of());
        Assertions.assertEquals(resultFour.size(), 0);
    }

    @Nested
    @DisplayName("getBoardList 메서드는")
    class GetSearchBoard {

        @Test
        @DisplayName("성공적으로 스크롤을 사용할 수 있다")
        void successScroll() {

            for (int i = 0; 15 > i; i++) {
                Product product1 = fixtureProduct(Map.of(
                    "category", Category.COOKIE,
                    "veganTag", true
                ));
                fixtureBoard(Map.of(
                    "title", "비건베이커리",
                    "productList", List.of(product1),
                    "price", 2000,
                    "isDeleted", false
                ));

                Product product2 = fixtureProduct(Map.of(
                    "category", Category.COOKIE,
                    "veganTag", true
                ));
                fixtureBoard(Map.of(
                    "title", "비건베이커리",
                    "productList", List.of(product2),
                    "price", 2000,
                    "isDeleted", true
                ));
            }

            redisRepository.deleteAll();
            searchLoadService.cacheKeywords();

            FilterRequest filterRequest = FilterRequest.builder()
                .category(Category.COOKIE)
                .highProteinTag(false)
                .ketogenicTag(false)
                .sugarFreeTag(false)
                .glutenFreeTag(false)
                .veganTag(true)
                .maxPrice(3000)
                .minPrice(0)
                .build();

            SortType sort = SortType.RECENT;
            String keyword = "비건베이커리";
            Long cursorId = null;
            Long memberId = null;

            SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                cursorId,
                memberId
            );

            Long nextCursor = searchCustomPage.getNextCursor();

            SearchCustomPage<SearchResponse> newSearchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                nextCursor,
                memberId
            );
            assertAll(
                () -> assertThat(newSearchCustomPage.getContent().getBoardResponseDtos()).hasSize(
                    5),
                () -> assertThat(newSearchCustomPage.getContent().getItemAllCount()).isEqualTo(14)
            );
        }

        @Test
        @DisplayName("성공적으로 원하는 키워드가 포함된 게시물읋 조회할 수 있다")
        void successKeyword() {

            fixtureBoard(Map.of(
                "title", "비건 베이커리",
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 선풍기 미니 베이커리",
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 맛있는 베이커리 음식",
                "isDeleted", false
            ));

            redisRepository.deleteAll();
            searchLoadService.cacheKeywords();

            FilterRequest filterRequest = FilterRequest.builder()
                .build();
            SortType sort = SortType.RECENT;
            String keyword = "비건베이커리";
            Long cursorId = null;
            Long memberId = null;

            SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                cursorId,
                memberId
            );

            assertAll(
                () -> assertThat(searchCustomPage.getContent().getBoardResponseDtos()).hasSize(
                    3),
                () -> assertThat(searchCustomPage.getContent().getItemAllCount()).isEqualTo(3)
            );
        }

        @Test
        @DisplayName("성공적으로 가격이 높은 순으로 상품을 조회할 수 있다")
        void successHighPriceSortting() {

            fixtureBoard(Map.of(
                "title", "비건 베이커리",
                "price", 2000,
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 선풍기 미니 베이커리",
                "price", 3000,
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 맛있는 베이커리 음식",
                "price", 1000,
                "isDeleted", false
            ));

            redisRepository.deleteAll();
            searchLoadService.cacheKeywords();

            FilterRequest filterRequest = FilterRequest.builder()
                .build();
            SortType sort = SortType.HIGH_PRICE;
            String keyword = "비건베이커리";
            Long cursorId = null;
            Long memberId = null;

            SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                cursorId,
                memberId
            );

            assertAll(
                () -> assertThat(
                    searchCustomPage.getContent().getBoardResponseDtos().get(0)
                        .getPrice()).isEqualTo(3000),
                () -> assertThat(
                    searchCustomPage.getContent().getBoardResponseDtos().get(1)
                        .getPrice()).isEqualTo(2000),
                () -> assertThat(searchCustomPage.getContent().getBoardResponseDtos().get(2)
                    .getPrice()).isEqualTo(1000)
            );
        }

        @Test
        @DisplayName("성공적으로 가격이 낮은 순으로 상품을 조회할 수 있다")
        void successLowPriceSortting() {

            fixtureBoard(Map.of(
                "title", "비건 베이커리",
                "price", 2000,
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 선풍기 미니 베이커리",
                "price", 3000,
                "isDeleted", false
            ));

            fixtureBoard(Map.of(
                "title", "비건 맛있는 베이커리 음식",
                "price", 1000,
                "isDeleted", false
            ));

            redisRepository.deleteAll();
            searchLoadService.cacheKeywords();

            FilterRequest filterRequest = FilterRequest.builder()
                .build();
            SortType sort = SortType.LOW_PRICE;
            String keyword = "비건베이커리";
            Long cursorId = null;
            Long memberId = null;

            SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                cursorId,
                memberId
            );

            assertAll(
                () -> assertThat(
                    searchCustomPage.getContent().getBoardResponseDtos().get(2)
                        .getPrice()).isEqualTo(3000),
                () -> assertThat(
                    searchCustomPage.getContent().getBoardResponseDtos().get(1)
                        .getPrice()).isEqualTo(2000),
                () -> assertThat(searchCustomPage.getContent().getBoardResponseDtos().get(0)
                    .getPrice()).isEqualTo(1000)
            );
        }

        // 추후에 구현해야할 테스트
        // 리뷰 많은 순
        // 리뷰 많은 순
        // 리뷰 인기 순
        // 추천 순

    }


    @Test
    @DisplayName("기본으로 등록된 베스트 키워드를 가져올 수 있다")
    void getBestKeyword() {
        String BEST_KEYWORD_KEY = "keyword";
        var bestKewords = redisRepository.getStringList(RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY);

        assertThat(bestKewords).isEqualTo(List.of("글루텐프리", "비건", "저당", "키토제닉"));
    }
}
