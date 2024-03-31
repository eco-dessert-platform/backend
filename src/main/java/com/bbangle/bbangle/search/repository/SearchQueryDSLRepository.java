package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.member.domain.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchQueryDSLRepository {

    SearchBoardResponse getSearchedBoard(Long memberId, List<Long> boardIds, SearchBoardRequest searchBoardRequest, Pageable pageable);
    List<StoreResponseDto> getSearchedStore(Long memberId, List<Long> storeIndexList, Pageable pageable);

    List<KeywordDto> getRecencyKeyword(Member member);

    String[] getBestKeyword();

    void markAsDeleted(String keyword, Member member);

}
