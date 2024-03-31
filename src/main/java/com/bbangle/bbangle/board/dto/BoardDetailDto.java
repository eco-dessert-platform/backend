package com.bbangle.bbangle.board.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BoardDetailDto(
        Long boardId,
        String thumbnail,
        List<BoardImgDto> images,
        String title,
        int price,
        BoardAvailableDayDto orderAvailableDays,
        String purchaseUrl,
        Boolean isWished,
        Boolean isBundled,
        List<DetailResponseDto> detail,
        List<String> tags,
        List<ProductDto> products
) {}
