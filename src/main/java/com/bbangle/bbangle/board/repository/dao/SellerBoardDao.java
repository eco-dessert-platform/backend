package com.bbangle.bbangle.board.repository.dao;

import com.bbangle.bbangle.board.domain.SaleStatus;

public record SellerBoardDao(
    Long boardId,
    String thumbnailUrl,
    String title,
    int price,
    int discountPrice,
    int discountValue,
    Integer deliveryFee,
    Integer freeShippingConditions,
    SaleStatus saleStatus
) {

}
