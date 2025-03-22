package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchQueryDSLRepository {

    SearchInfo.CursorCondition getCursorCondition(Long cursorId);

    List<Board> getBoards(SearchCommand.Main command, SearchInfo.CursorCondition condition);

    Long getAllCount(
            SearchCommand.Main command,
            SearchInfo.CursorCondition condition
    );

    List<KeywordDto> getRecencyKeyword(Long memberId);

    String[] getBestKeyword(LocalDateTime beforeOneDayTime);

    void markAsDeleted(String keyword, Long memberId);

}
