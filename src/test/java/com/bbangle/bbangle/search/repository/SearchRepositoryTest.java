package com.bbangle.bbangle.search.repository;

import static com.bbangle.bbangle.fixture.BoardStatisticFixture.newBoardStatisticWithBasicScore;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.fixture.FixtureConfig;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.SearchFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.service.SearchLoadService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(FixtureConfig.class)
class SearchRepositoryTest extends AbstractIntegrationTest {

    private static final int ONEDAY = 24;
    private static final Long NULL_CURSOR = null;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    SearchLoadService searchLoadService;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    SearchFixture searchFixture;

    @AfterEach
    void deleteAllEntity() {
        redisRepository.deleteAll();
        searchRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @ParameterizedTest
    @EnumSource(value = SortType.class, names = {"RECENT", "LOW_PRICE", "HIGH_PRICE"})
    @DisplayName("getBoardResponseList 메서드는 게시물을 성공적으로 조회할 수 있다")
    void getBoardResponseListFromBoard(SortType sort) {

        List<Long> boardIds = new ArrayList<>();
        for (int i = 0; 3 > i; i++) {
            Product product = fixtureProduct(Map.of("glutenFreeTag", true));
            boardIds.add(
                fixtureBoard(Map.of("productList", List.of(product), "isDeleted", false)).getId());
        }

        FilterRequest filterRequest = FilterRequest.builder()
            .glutenFreeTag(true)
            .build();

        List<BoardResponseDao> boardResponseDaos = searchRepository.getBoardResponseList(boardIds,
            filterRequest, sort, NULL_CURSOR);
        assertThat(boardResponseDaos).hasSize(boardIds.size());
    }

    @ParameterizedTest
    @EnumSource(value = SortType.class, names = {"RECOMMEND", "MOST_WISHED", "MOST_REVIEWED",
        "HIGHEST_RATED"})
    @DisplayName("getBoardResponseList 메서드는 점수가 계산된 게시물을 성공적으로 조회할 수 있다")
    void getBoardResponseListFromBoardStatistic(SortType sort) {

        List<Long> boardIds = new ArrayList<>();
        for (int i = 0; 3 > i; i++) {
            double score = i;

            Product product = fixtureProduct(Map.of("glutenFreeTag", true));
            Board board = fixtureBoard(Map.of("productList", List.of(product), "isDeleted", false));
            BoardStatistic boardStatistic = newBoardStatisticWithBasicScore(board, score);
            boardStatisticRepository.save(boardStatistic);
            boardIds.add(board.getId());
        }

        FilterRequest filterRequest = FilterRequest.builder()
            .glutenFreeTag(true)
            .build();

        List<BoardResponseDao> boardResponseDaos = searchRepository.getBoardResponseList(boardIds,
            filterRequest, sort, NULL_CURSOR);
        assertThat(boardResponseDaos).hasSize(boardIds.size());
    }

    @ParameterizedTest
    @EnumSource(value = SortType.class, names = {"RECENT", "LOW_PRICE", "HIGH_PRICE"})
    @DisplayName("getAllCount 메서드는 검색된 게시글의 전체 개수를 조회할 수 있다")
    void getAllCountFromBoard(SortType sort) {

        List<Long> boardIds = new ArrayList<>();
        for (int i = 0; 3 > i; i++) {
            Product product = fixtureProduct(Map.of("glutenFreeTag", true));
            boardIds.add(
                fixtureBoard(Map.of("productList", List.of(product), "isDeleted", false)).getId());
        }

        FilterRequest filterRequest = FilterRequest.builder()
            .glutenFreeTag(true)
            .build();

        Long count = searchRepository.getAllCount(boardIds, filterRequest, sort);
        assertThat(count).isEqualTo(boardIds.size());
    }

    @ParameterizedTest
    @EnumSource(value = SortType.class, names = {"RECOMMEND", "MOST_WISHED", "MOST_REVIEWED",
        "HIGHEST_RATED"})
    @DisplayName("getAllCount 메서드는 검색된 게시글의 전체 개수를 조회할 수 있다")
    void getAllCountFromBoardStatistic(SortType sort) {

        List<Long> boardIds = new ArrayList<>();
        for (int i = 0; 3 > i; i++) {
            double score = i;

            Product product = fixtureProduct(Map.of("glutenFreeTag", true));
            Board board = fixtureBoard(Map.of("productList", List.of(product), "isDeleted", false));
            BoardStatistic boardStatistic = newBoardStatisticWithBasicScore(board, score);
            boardStatisticRepository.save(boardStatistic);
            boardIds.add(board.getId());
        }

        FilterRequest filterRequest = FilterRequest.builder()
            .glutenFreeTag(true)
            .build();

        Long count = searchRepository.getAllCount(boardIds, filterRequest, sort);
        assertThat(count).isEqualTo(boardIds.size());
    }

    @Test
    @DisplayName("getBestKeyword 메서드는 베스트 게시물 검색어를 조회할 수 있다")
    void getBestKeywordTest() {
        Member member1 = memberRepository.save(MemberFixture.createKakaoMember());
        Set<String> keywords = new HashSet<>();

        while (keywords.size() < 7) {
            keywords.add(searchFixture.create(member1.getId()).getKeyword());
        }
        LocalDateTime oneDayAgo = getOneDayAgo();
        String[] bestKeywords = searchRepository.getBestKeyword(oneDayAgo);

        for (String keyword : bestKeywords) {
            assertThat(keyword).isIn(keywords);
        }
    }

    private LocalDateTime getOneDayAgo() {
        return LocalDateTime.now()
            .minusHours(ONEDAY);
    }

    @Test
    @DisplayName("getRecencyKeyword 메서드는 최근 키워드를 조회할 수 있다")
    void getRecentKeywordTest() {
        int LIMIT_KEYWORD_COUNT = 7;

        Member member1 = memberRepository.save(MemberFixture.createKakaoMember());
        Set<String> keywords = new HashSet<>();

        while (keywords.size() < 9) {
            keywords.add(searchFixture.create(member1.getId()).getKeyword());
        }

        List<KeywordDto> recencyKewords = searchRepository.getRecencyKeyword(member1.getId());
        assertThat(recencyKewords).hasSize(LIMIT_KEYWORD_COUNT);
    }

}
