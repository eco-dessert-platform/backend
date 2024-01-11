package com.bbangle.bbangle.dto;


import java.util.List;
import lombok.Builder;

@Builder
public record BoardResponseDto(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        int price,
        ProductTagDto tagDto
) { }

