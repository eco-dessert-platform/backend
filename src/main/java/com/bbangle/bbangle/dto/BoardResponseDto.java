package com.bbangle.bbangle.dto;

import java.util.HashMap;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
public record BoardResponseDto(
    Long boardId,
    Long storeId,
    String storeName,
    String thumbnail,
    String title,
    int price,
    Boolean isWished,
    List<String> tags
) {

}
