package com.bbangle.bbangle.search.dto.response;

import java.util.List;

import com.bbangle.bbangle.search.dto.KeywordDto;
import lombok.Builder;

@Builder
public record RecencySearchResponse(
    List<KeywordDto> content
) {

}
