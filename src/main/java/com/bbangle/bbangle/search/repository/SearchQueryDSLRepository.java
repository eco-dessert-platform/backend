package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.member.domain.Member;

import java.util.List;

public interface SearchQueryDSLRepository {

    List<BoardResponseDao> getBoardResponseList(
        List<Long> boardIds,
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    );

    Long getAllCount(
        List<Long> boardIds,
        FilterRequest filterRequest,
        SortType sort
    );

    List<KeywordDto> getRecencyKeyword(Member member);

    String[] getBestKeyword();

    void markAsDeleted(String keyword, Member member);

}
