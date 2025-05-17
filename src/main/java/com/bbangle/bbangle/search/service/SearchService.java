package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.MemberSegment;
import com.bbangle.bbangle.board.repository.MemberSegmentRepository;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.dto.SearchCommand.Main;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.bbangle.bbangle.search.service.mapper.SearchInfoMapper;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.search.validation.SearchValidation.checkNullOrEmptyKeyword;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

        private static final int LIMIT_KEYWORD_COUNT = 20;
        private static final Long ANONYMOUS_MEMBER_ID = 1L;
        private final SearchRepository searchRepository;
        private final MemberSegmentRepository memberSegmentRepository;
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

        public SearchInfo.BoardsInfo getBoardList(Main command) {

                SearchInfo.CursorCondition cursorCondition = Objects.nonNull(command.cursorId()) ?
                    searchRepository.getCursorCondition(command.cursorId()) :
                    SearchInfo.CursorCondition.empty();

                return command.isExcludedProduct() ?
                    getRecommendBoardList(command, cursorCondition) :
                    getDefaultBoardList(command, cursorCondition);
        }

        private SearchInfo.BoardsInfo getDefaultBoardList(Main command, SearchInfo.CursorCondition cursorCondition) {
                List<Board> boards = searchRepository.getBoards(command, cursorCondition);
                Long boardCount = searchRepository.getAllCount(command, cursorCondition);
                return searchInfoMapper.toBoardsInfo(boards, boardCount);
        }

        private SearchInfo.BoardsInfo getRecommendBoardList(Main command, SearchInfo.CursorCondition cursorCondition) {
                MemberSegment memberSegment = memberSegmentRepository.findByMemberId(command.memberId())
                    .orElseThrow(() -> new BbangleException(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND));
                List<Board> boards = searchRepository.getRecommendBoardList(command, cursorCondition, memberSegment);
                Long boardCount = searchRepository.getRecommendAllCount(command, cursorCondition, memberSegment);
                return searchInfoMapper.toBoardsInfo(boards, boardCount);
        }

        public CursorPagination<SearchInfo.Select> convertBoardsToCursorPagination(SearchInfo.BoardsInfo boardsInfo, Map<Long, Boolean> boardWishedMap) {

                List<SearchInfo.Select> selects = boardsInfo.getBoards().stream()
                    .map(board -> searchInfoMapper.toSearchSelectInfo(board, boardWishedMap.getOrDefault(board.getId(), false)))
                    .toList();

                return CursorPagination.of(
                    selects,
                    BOARD_PAGE_SIZE,
                    boardsInfo.getBoardCount(),
                    SearchInfo.Select::getBoardId
                );
        }

        @Transactional(readOnly = true)
        public RecencySearchResponse getRecencyKeyword(Long memberId) {

                if (Objects.isNull(memberId)) {
                        return RecencySearchResponse.getEmpty();
                }

                List<KeywordDto> keywords = searchRepository.getRecencyKeyword(memberId);

                return RecencySearchResponse.of(keywords);
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
