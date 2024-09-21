package com.bbangle.bbangle.search.dto.response;

import java.util.Collections;
import java.util.List;

import com.bbangle.bbangle.search.dto.KeywordDto;
import lombok.Builder;

@Builder
public record RecencySearchResponse(
    List<KeywordDto> content
) {

    public static RecencySearchResponse of(List<KeywordDto> kewords) {
        return RecencySearchResponse.builder()
            .content(kewords)
            .build();
    }

    public static RecencySearchResponse getEmpty() {
        return RecencySearchResponse.builder()
            .content(Collections.emptyList())
            .build();
    }
}
