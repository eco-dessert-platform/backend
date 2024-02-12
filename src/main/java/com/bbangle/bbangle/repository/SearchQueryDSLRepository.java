package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.dto.BoardResponseDto;
import com.bbangle.bbangle.dto.KeywordDto;
import com.bbangle.bbangle.model.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface SearchQueryDSLRepository {
    Slice<BoardResponseDto> getSearchResult(List<Long> boardIdes, Pageable pageable);
    List<KeywordDto> getRecencyKeyword(Member member);
    String[] getBestKeyword();
}
