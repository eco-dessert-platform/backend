package com.bbangle.bbangle.service;

import com.bbangle.bbangle.dto.RecencySearchResponse;
import com.bbangle.bbangle.dto.SearchResponseDto;

import java.util.List;

public interface SearchService {
    void initSetting();
    void updateRedisAtBestKeyword();

    SearchResponseDto getSearchResult(int storePage, int boarPage, String keyword);
    void saveKeyword(Long memberId,String keyword);

    RecencySearchResponse getRecencyKeyword(Long memberId);

    Boolean deleteRecencyKeyword(String keyword, Long memberId);

    List<String> getBestKeyword();

    List<String> getAutoKeyword(String keyword);
}
