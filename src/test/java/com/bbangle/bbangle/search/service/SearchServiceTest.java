package com.bbangle.bbangle.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.FixtureConfig;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.SearchFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(FixtureConfig.class)
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
    @Autowired
    ProductFixture productFixture;
    @Autowired
    SearchFixture searchFixture;
    @MockBean
    KeywordUtil keywordUtil;


    @Test
    @DisplayName("자동완성 알고리즘에 값을 저장하면 정상적으로 저장한 값을 불러올 수 있다")
    void trieUtilTest() {
        autoCompleteUtil.insert("비건 베이커리");
        autoCompleteUtil.insert("비건");
        autoCompleteUtil.insert("비건 베이커리 짱짱");
        autoCompleteUtil.insert("초코송이");

        var resultOne = autoCompleteUtil.autoComplete("초", 1);
        assertThat(resultOne).hasSize(1).contains("초코송이");

        var resultTwo = autoCompleteUtil.autoComplete("비", 2);
        assertThat(resultTwo).hasSize(2).contains("비건", "비건 베이커리");

        var resultThree = autoCompleteUtil.autoComplete("비", 3);
        assertThat(resultThree).hasSize(3).contains("비건", "비건 베이커리", "비건 베이커리 짱짱");

        var resultFour = autoCompleteUtil.autoComplete("바", 3);
        assertThat(resultFour).isEmpty();
    }

    @Nested
    @DisplayName("getBoardList 메서드는")
    class GetSearchBoard {

        private List<Long> boardIds;

        @ParameterizedTest
        @EnumSource(value = SortType.class, names = {"RECENT", "LOW_PRICE", "HIGH_PRICE",
            "RECOMMEND", "MOST_WISHED", "MOST_REVIEWED", "HIGHEST_RATED"})
        @DisplayName("성공적으로 스크롤을 사용할 수 있다")
        void successScroll(SortType sort) {

            saveData();

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

            String keyword = "비건베이커리";
            Long cursorId = null;
            Long memberId = null;

            when(keywordUtil.getBoardIds(any())).thenReturn(boardIds);

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
                () -> assertThat(newSearchCustomPage.getContent().getBoards()).hasSize(
                    5),
                () -> assertThat(newSearchCustomPage.getContent().getItemAllCount()).isEqualTo(15)
            );

            List<BoardResponseDto> resultAll = new ArrayList<>(searchCustomPage.getContent().getBoards());
            resultAll.addAll(newSearchCustomPage.getContent().getBoards());

            switch (sort) {
                case RECENT:
                    assertThat(resultAll.stream().map(BoardResponseDto::getBoardId)).isSortedAccordingTo(
                        Comparator.reverseOrder());
                    break;
                case LOW_PRICE:
                    assertThat(resultAll.stream().map(BoardResponseDto::getPrice)).isSortedAccordingTo(
                        Comparator.naturalOrder());
                    break;
                case HIGH_PRICE:
                    assertThat(resultAll.stream().map(BoardResponseDto::getPrice)).isSortedAccordingTo(
                        Comparator.reverseOrder());
                    break;
            }
        }

        void saveData() {
            boardIds = new ArrayList<>();
            for (long i = 0L; 15 > i; i++) {

                Board board = fixtureBoard(Map.of(
                    "id", i + 1,
                    "title", "비건베이커리",
                    "price", 2000,
                    "isDeleted", false,
                    "products", List.of()
                ));

                productFixture.veganCookie(board);

                double score = i + 1;
                BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatisticWithBasicScore(
                    board, score);
                boardStatisticRepository.save(boardStatistic);
                boardIds.add(board.getId());
            }

            for (long i = 15L; 30 > i; i++) {
                Board board = fixtureBoard(Map.of(
                    "id", i + 1,
                    "title", "비건베이커리",
                    "price", 2000,
                    "isDeleted", true,
                    "products", List.of()
                ));

                productFixture.veganCookie(board);

                double score = i + 1;
                BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatisticWithBasicScore(
                    board, score);
                boardStatisticRepository.save(boardStatistic);
                boardIds.add(board.getId());
            }
        }
    }

    @Test
    @DisplayName("저장된 키워드를 삭제할 수 있다")
    void deleteKeyword() {
        Member member1 = memberRepository.save(MemberFixture.createKakaoMember());
        Search search = searchFixture.create(member1.getId());

        searchService.deleteRecencyKeyword(search.getKeyword(), member1.getId());
        Optional<Search> checkSearch = searchRepository.findById(search.getId());

        assertThat(checkSearch).isPresent();
        assertThat(checkSearch.get().isDeleted()).isTrue();
    }
}
