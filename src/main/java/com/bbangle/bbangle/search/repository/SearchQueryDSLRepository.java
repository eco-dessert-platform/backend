package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.service.dto.SearchCommand;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchQueryDSLRepository {

    List<Board> getBoardResponseList(SearchCommand.Main command);

    Long getAllCount(
        String keyword,
        FilterRequest filterRequest
    );

    List<KeywordDto> getRecencyKeyword(Long memberId);

    String[] getBestKeyword(LocalDateTime beforeOneDayTime);

    void markAsDeleted(String keyword, Long memberId);

}
