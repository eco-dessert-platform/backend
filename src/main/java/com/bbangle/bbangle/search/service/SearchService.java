package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.search.dto.response.SearchStoreResponse;

import java.util.List;

public interface SearchService {

    void initSetting();

    void updateRedisAtBestKeyword();
    void saveKeyword(Long memberId,String keyword);

    SearchBoardResponse getSearchBoardDtos(Long memberId, SearchBoardRequest searchBoardRequest);

    SearchStoreResponse getSearchStoreDtos(Long memberId, int storePage, String keyword);

    RecencySearchResponse getRecencyKeyword(Long memberId);

    Boolean deleteRecencyKeyword(String keyword, Long memberId);

    List<String> getBestKeyword();

    List<String> getAutoKeyword(String keyword);
}
