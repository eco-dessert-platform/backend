package com.bbangle.bbangle.board.customer.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarityBoardResponse {

    Long boardId;
    Long storeId;
    String thumbnail;
    String storeName;
    String title;
    Integer discountRate;
    Integer price;
    BigDecimal reviewRate;
    Long reviewCount;
    List<String> tags;
    Boolean isWished;
    Boolean isBundled;
    Boolean isSoldOut;
}
