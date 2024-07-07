package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.dto.response.util.SearchPageGenerator;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import java.time.LocalDateTime;
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

    private static final String BEST_KEYWORD_KEY = "keyword";
    private static final int LIMIT_KEYWORD_COUNT = 10;
    private static final Boolean DEFAULT_BOARD = false;
    private final SearchRepository searchRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final RedisRepository redisRepository;
    private final AutoCompleteUtil autoCompleteUtil;
    private final KeywordUtil keywordUtil;

    @Transactional
    public void saveKeyword(Long memberId, String keyword) {
        var member = Member.builder()
            .id(memberId)
            .build();

        var search = Search.builder()
            .member(member)
            .keyword(keyword)
            .createdAt(LocalDateTime.now())
            .build();

        searchRepository.save(search);
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

        if (boardCount > LIMIT_KEYWORD_COUNT) {
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
            .getBoardResponseDtos()
            .stream()
            .filter(board -> likedContentIds.contains(board.getBoardId()))
            .forEach(board -> board.updateLike(true));
    }

    private List<Long> extractIds(
        SearchCustomPage<SearchResponse> searchCustomPage
    ) {
        return searchCustomPage.getContent()
            .getBoardResponseDtos()
            .stream()
            .map(BoardResponseDto::getBoardId)
            .toList();
    }

    @Transactional(readOnly = true)
    public RecencySearchResponse getRecencyKeyword(Long memberId) {
        Member member = Member.builder()
            .id(memberId)
            .build();

        return memberId == 1L ?
            RecencySearchResponse.getEmpty() :
            RecencySearchResponse.builder()
                .content(searchRepository.getRecencyKeyword(member))
                .build();
    }

    @Transactional
    public Boolean deleteRecencyKeyword(String keyword, Long memberId) {
        Member member = Member.builder()
            .id(memberId)
            .build();

        searchRepository.markAsDeleted(keyword, member);
        return true;
    }

    public List<String> getBestKeyword() {
        return redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );
    }

    public List<String> getAutoKeyword(String keyword) {
        return autoCompleteUtil.autoComplete(keyword, LIMIT_KEYWORD_COUNT);
    }
}
