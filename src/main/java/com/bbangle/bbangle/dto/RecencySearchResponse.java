package com.bbangle.bbangle.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecencySearchResponse (
        List<KeywordDto> content
){

}
