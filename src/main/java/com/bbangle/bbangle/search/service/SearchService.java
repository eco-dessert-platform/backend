package com.bbangle.bbangle.search.service;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.search.validation.SearchValidation.checkNullOrEmptyKeyword;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.page.ProcessedDataCursor;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.bbangle.bbangle.search.service.mapper.SearchInfoMapper;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;

import java.util.Collections;
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
    private final SearchInfoMapper searchInfoMapper;

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
    public ProcessedDataCursor<SearchInfo.Select, SearchInfo.SearchBoardPage> getBoardList(SearchCommand.Main command) {

        List<Board> boards = searchRepository.getBoardResponseList(command);
        Long boardCount = searchRepository.getAllCount(command.keyword(), command.filterRequest());

        List<SearchInfo.Select> selects;
        Map<Long, Boolean> boardWishedMap = Objects.nonNull(command.memberId())
                ? getBoardWishedMap(command.memberId(), boards)
                : Collections.emptyMap();

        selects = boards.stream()
                .map(board -> searchInfoMapper.toSearchSelectInfo(board, boardWishedMap.getOrDefault(board.getId(), false)))
                .toList();

        return ProcessedDataCursor.of(
                selects,
                BOARD_PAGE_SIZE,
                SearchInfo.Select::getBoardId,
                board -> new SearchInfo.SearchBoardPage(board, boardCount)
        );
    }


    private Map<Long, Boolean> getBoardWishedMap(Long memberId, List<Board> boards) {
        List<Long> boardIds = boards.stream().map(Board::getId).toList();
        Map<Long, Boolean> boardMap = boardIds.stream().collect(Collectors.toMap(id -> id, id -> false));
        boardRepository.getLikedContentsIds(boardIds, memberId).forEach(boardId -> boardMap.put(boardId, true));
        return boardMap;
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
