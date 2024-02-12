package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.dto.BoardResponseDto;
import com.bbangle.bbangle.dto.KeywordDto;
import com.bbangle.bbangle.dto.StoreResponseDto;
import com.bbangle.bbangle.model.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchQueryDSLRepository {
    List<BoardResponseDto> getSearchResult(List<Long> boardIdes);
    List<StoreResponseDto> getSearchedStore(List<Long> storeIndexList);
    List<KeywordDto> getRecencyKeyword(Member member);
    String[] getBestKeyword();
    void markAsDeleted(String keyword, Member member);
}
