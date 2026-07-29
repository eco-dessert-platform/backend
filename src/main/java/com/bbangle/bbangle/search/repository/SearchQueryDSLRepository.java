package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.MemberSegment;
import com.bbangle.bbangle.common.aop.ExecutionTimeLog;
import com.bbangle.bbangle.search.customer.dto.KeywordDto;
import com.bbangle.bbangle.search.customer.service.dto.SearchCommand;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import java.time.LocalDateTime;
import java.util.List;

public interface SearchQueryDSLRepository {

    @ExecutionTimeLog
    SearchInfo.CursorCondition getCursorCondition(Long cursorId);

    @ExecutionTimeLog
    List<Board> getBoards(SearchCommand.Main command, SearchInfo.CursorCondition condition);

    @ExecutionTimeLog
    Long getAllCount(SearchCommand.Main command, SearchInfo.CursorCondition condition);

    @ExecutionTimeLog
    List<Board> getRecommendBoardList(
        SearchCommand.Main command,
        SearchInfo.CursorCondition condition,
        MemberSegment memberSegment
    );

    @ExecutionTimeLog
    Long getRecommendAllCount(
        SearchCommand.Main command,
        SearchInfo.CursorCondition condition,
        MemberSegment memberSegment
    );

    List<KeywordDto> getRecencyKeyword(Long memberId);

    String[] getBestKeyword(LocalDateTime beforeOneDayTime);

    void markAsDeleted(String keyword, Long memberId);

}
