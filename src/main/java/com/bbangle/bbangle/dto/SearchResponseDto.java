package com.bbangle.bbangle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@AllArgsConstructor
@Getter
public class SearchResponseDto {
    Slice<BoardResponseDto> boards;
    Slice<StoreResponseDto> stores;
}
