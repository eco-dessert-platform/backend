package com.bbangle.bbangle.search.service;

import static com.bbangle.bbangle.search.validation.SearchValidation.checkNullOrEmptyKeyword;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.dto.response.util.SearchPageGenerator;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int LIMIT_KEYWORD_COUNT = 10;
    private static final Long ANONYMOUS_MEMBER_ID = 1L;
    private static final Boolean DEFAULT_BOARD = false;

    private final SearchRepository searchRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
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
        List<Long> searchedBoardIndexs = keywordUtil.getBoardIds(keyword);
        List<BoardResponseDao> boards = searchRepository.getBoardResponseList(
            searchedBoardIndexs,
            filterRequest,
            sort,
            cursorId);

        Long boardCount = searchRepository.getAllCount(searchedBoardIndexs, filterRequest, sort);

        if (boards.size() > LIMIT_KEYWORD_COUNT) {
            boardCount--;
        }

        SearchCustomPage<SearchResponse> searchCustomPage = SearchPageGenerator.getBoardPage(
            boards,
            DEFAULT_BOARD,
            boardCount);

        if (Objects.nonNull(memberId) && memberRepository.existsById(memberId)) {
            updateLikeStatus(searchCustomPage, memberId);
        }

        return searchCustomPage;
    }

    private void updateLikeStatus(
        SearchCustomPage<SearchResponse> searchCustomPage,
        Long memberId
    ) {
        List<Long> responseList = extractIds(searchCustomPage);
        List<Long> likedContentIds = boardRepository.getLikedContentsIds(responseList, memberId);

        searchCustomPage.getContent()
            .getBoards()
            .stream()
            .filter(board -> likedContentIds.contains(board.getBoardId()))
            .forEach(board -> board.updateLike(true));
    }

    private List<Long> extractIds(
        SearchCustomPage<SearchResponse> searchCustomPage
    ) {
        return searchCustomPage.getContent()
            .getBoards()
            .stream()
            .map(BoardResponseDto::getBoardId)
            .toList();
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
