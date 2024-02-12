package com.bbangle.bbangle.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchStoreDto(
        List<StoreResponseDto> content,
        int itemCount,
        int pageNumber,
        int pageSize
){
}
