package com.bbangle.bbangle.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class SearchServiceMockTest extends AbstractIntegrationTest {

    private static final Long ANONYMOUS_MEMBER_ID = null;
    private static final Long MEMBER_ID = 2L;
    @MockBean
    SearchRepository searchRepository;
    @MockBean
    KeywordUtil keywordUtil;
    @Autowired
    SearchService searchService;

    @Nested
    @DisplayName("getBoardList 메서드는")
    class GetSearchBoard {

        private FilterRequest filterRequest;
        private SortType sort;
        private String keyword;
        private Long cursorId;
        private Long memberId;
        private BoardResponseDao boardResponseDao1;
        private BoardResponseDao boardResponseDao2;

        @BeforeEach
        void setUp() {
            filterRequest = new FilterRequest(
                true,
                true,
                false,
                false,
                false,
                Category.COOKIE,
                null,
                null,
                false
            );
            sort = SortType.RECOMMEND;
            keyword = "testKeyword";
            cursorId = 1L;
            memberId = 2L;

            boardResponseDao1 = new BoardResponseDao(
                2L,
                1L,
                "testStoreName",
                "testThumbnail",
                keyword,
                3000,
                Category.COOKIE,
                true,
                true,
                false,
                false,
                false,
                BigDecimal.ONE,
                4,
                LocalDateTime.now(),
                false,
                4);
            boardResponseDao2 = new BoardResponseDao(
                3L,
                1L,
                "testStoreName",
                "testThumbnail",
                keyword,
                3000,
                Category.COOKIE,
                true,
                true,
                false,
                false,
                false,
                BigDecimal.ONE,
                4,
                LocalDateTime.now(),
                false,
                4);
        }

        @Test
        @DisplayName("검색 결과 게시물을 조회할 수 있다")
        void successScroll() {

            List<Long> searchedBoardIndexs = Arrays.asList(1L, 2L, 3L);

            List<BoardResponseDao> boards = Arrays.asList(boardResponseDao1, boardResponseDao2);
            Long boardCount = 2L;

            when(keywordUtil.getBoardIds(keyword)).thenReturn(searchedBoardIndexs);
            when(searchRepository.getBoardResponseList(searchedBoardIndexs, filterRequest, sort,
                cursorId)).thenReturn(boards);
            when(searchRepository.getAllCount(searchedBoardIndexs, filterRequest, sort)).thenReturn(
                boardCount);

            SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
                filterRequest,
                sort,
                keyword,
                cursorId,
                memberId
            );

            assertAll(
                () -> assertThat(searchCustomPage.getNextCursor()).isEqualTo(-1L),
                () -> assertThat(searchCustomPage.getHasNext()).isFalse(),
                () -> assertThat(searchCustomPage.getContent().getBoards()).hasSize(2),
                () -> assertThat(searchCustomPage.getContent().getItemAllCount()).isEqualTo(
                    boardCount)
            );
        }
    }

    @Nested
    @DisplayName("최근 검색어 조회 테스트")
    class GetRecencyKeyword {

        @Test
        @DisplayName("익명 회원일 때 빈 응답을 반환해야 한다")
        void withAnonymousMember() {
            RecencySearchResponse response = searchService.getRecencyKeyword(ANONYMOUS_MEMBER_ID);

            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
        }

        @Test
        @DisplayName("유효한 회원일 때 최근 검색어를 반환해야 한다")
        void withValidMemberId() {
            Long validMemberId = 2L;
            List<KeywordDto> keywords = Arrays.asList(new KeywordDto("keyword1"),
                new KeywordDto("keyword2"));

            when(searchRepository.getRecencyKeyword(validMemberId)).thenReturn(keywords);

            RecencySearchResponse response = searchService.getRecencyKeyword(validMemberId);

            assertThat(response).isNotNull();
            assertThat(response.content()).isNotNull();
            assertThat(response.content()).hasSize(keywords.size());
        }

        @Test
        @DisplayName("검색어가 없을 때 빈 응답을 반환해야 한다")
        void withNoKeywords() {
            Long validMemberId = 2L;
            when(searchRepository.getRecencyKeyword(validMemberId)).thenReturn(
                Collections.emptyList());

            RecencySearchResponse response = searchService.getRecencyKeyword(validMemberId);

            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("검색어 저장 테스트")
    class SaveKeyword {

        @Test
        @DisplayName("비회원이면서 검색어가 존재한다면 성공적으로 검색어를 저장할 수 있다")
        void withAnonymousMember() {
            String keyword = "testKeyword";
            searchService.saveKeyword(ANONYMOUS_MEMBER_ID, keyword);

            Search search = Search.builder()
                .memberId(1L)
                .keyword(keyword)
                .build();

            when(searchRepository.save(any(Search.class))).thenReturn(search);

            verify(searchRepository).save(any(Search.class));
        }

        @Test
        @DisplayName("회원이면서 검색어가 존재한다면 성공적으로 검색어를 저장할 수 있다")
        void withValidMemberId() {
            String keyword = "testKeyword";
            searchService.saveKeyword(MEMBER_ID, keyword);

            Search search = Search.builder()
                .memberId(MEMBER_ID)
                .keyword(keyword)
                .build();

            when(searchRepository.save(any(Search.class))).thenReturn(search);

            verify(searchRepository).save(any(Search.class));
        }

        @Test
        @DisplayName("검색어가 존재하지 않는다면 400 에러를 반환한다")
        void withUnValidKeyword() {
            String keyword = "";
            assertThatThrownBy(() -> searchService.saveKeyword(MEMBER_ID, keyword))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("검색어가 Null이라면 400 에러를 반환한다")
        void unvalidKeywordToNull() {
            String keyword = null;
            assertThatThrownBy(() -> searchService.saveKeyword(MEMBER_ID, keyword))
                .isInstanceOf(BbangleException.class);
        }

    }

    @Test
    @DisplayName("인기 검색어 조회 테스트")
    void getBestKeyword() {
        List<String> bestKeywords = List.of("testBestKeyword1", "testBestKeyword2",
            "testBestKeyword3");

        when(keywordUtil.getBestKeyword()).thenReturn(bestKeywords);

        assertThat(searchService.getBestKeyword()).hasSize(3);
    }

}
