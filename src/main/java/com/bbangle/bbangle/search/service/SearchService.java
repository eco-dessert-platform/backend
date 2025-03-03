package com.bbangle.bbangle.search.service;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.search.validation.SearchValidation.checkNullOrEmptyKeyword;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.SearchBoardResponseDto;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int LIMIT_KEYWORD_COUNT = 20;
    private static final Long ANONYMOUS_MEMBER_ID = 1L;
    private static final Long NO_NEXT_CURSOR = -1L;

    private final SearchRepository searchRepository;
    private final BoardRepository boardRepository;
    private final AutoCompleteUtil autoCompleteUtil;
    private final KeywordUtil keywordUtil;

    @Transactional
    public void saveKeyword(Long memberId, String keyword) {
        checkNullOrEmptyKeyword(keyword);

        memberId = checkAnonymousId(memberId);
        Search search = Search.builder()
            .memberId(memberId)
            .keyword(keyword)
            .build();

        // 캐싱하여 특정 시간에 저장하는게 좋을까?
        searchRepository.save(search);
    }

    private Long checkAnonymousId(Long memberId) {
        if (Objects.isNull(memberId)) {
            return ANONYMOUS_MEMBER_ID;
        }

        return memberId;
    }

    @Transactional(readOnly = true)
    public SearchCustomPage<SearchResponse> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        String keyword,
        Long cursorId,
        Long memberId
    ) {
        List<SearchBoardResponseDto> boards = new ArrayList<>(
            searchRepository.getBoardResponseList(keyword, filterRequest, sort, cursorId)
                .stream()
                .map(SearchBoardResponseDto::new)
                .toList()
        );
        addBoardWished(memberId, boards);
        Long boardCount = searchRepository.getAllCount(keyword, filterRequest);

        return getSearchBoardPage(
            boards,
            boardCount);
    }

    private void addBoardWished(Long memberId, List<SearchBoardResponseDto> boards) {
        if (Objects.isNull(memberId)) {
            return;
        }

        List<Long> boardIds = boards.stream().map(SearchBoardResponseDto::getBoardId).toList();
        Map<Long, Boolean> boardMap = boardIds.stream()
            .collect(Collectors.toMap(id -> id, id -> false));

        boardRepository.getLikedContentsIds(boardIds, memberId)
            .stream().forEach(boardId -> boardMap.put(boardId, true));

        boards.stream().forEach(searchBoardResponseDto -> searchBoardResponseDto.updateWished(
            boardMap.get(searchBoardResponseDto.getBoardId())));
    }

    public static SearchCustomPage<SearchResponse> getSearchBoardPage(
        List<SearchBoardResponseDto> boards,
        Long allItemCount
    ) {
        if (boards.isEmpty()) {
            return SearchCustomPage.emptyPage();
        }

        Long nextCursor = NO_NEXT_CURSOR;
        boolean hasNext = false;
        if (boards.size() > BOARD_PAGE_SIZE) {
            int endBoardIndex = boards.size() - 1;

            hasNext = true;
            nextCursor = boards.get(endBoardIndex).getBoardId() - 1;
            boards.remove(endBoardIndex);
        }
        boards = boards.stream().limit(BOARD_PAGE_SIZE).toList();

        SearchResponse searchResponse = SearchResponse.of(boards, allItemCount);

        return SearchCustomPage.from(searchResponse, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public RecencySearchResponse getRecencyKeyword(Long memberId) {

        if (Objects.isNull(memberId)) {
            return RecencySearchResponse.getEmpty();
        }

        List<KeywordDto> kewords = searchRepository.getRecencyKeyword(memberId);

        return RecencySearchResponse.of(kewords);
    }

    @Transactional
    public void deleteRecencyKeyword(String keyword, Long memberId) {
        searchRepository.markAsDeleted(keyword, memberId);
    }

    public List<String> getBestKeyword() {
        return keywordUtil.getBestKeyword();
    }

    public List<String> getAutoKeyword(String keyword) {
        return autoCompleteUtil.autoComplete(keyword, LIMIT_KEYWORD_COUNT);
    }
}
