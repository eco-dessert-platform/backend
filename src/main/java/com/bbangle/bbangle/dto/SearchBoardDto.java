package com.bbangle.bbangle.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchBoardDto(
        List<BoardResponseDto> content,
        int itemCount,
        int pageNumber,
        int pageSize
){
}
