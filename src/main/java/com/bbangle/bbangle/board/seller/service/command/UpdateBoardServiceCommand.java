package com.bbangle.bbangle.board.seller.service.command;

import java.util.List;
import lombok.Builder;

@Builder
public record UpdateBoardServiceCommand(
    Long sellerId,
    Long boardId,
    String title,
    Boolean isFresh,
    String productionStartTime,
    Integer price,
    Integer discountValue,
    String discountType,
    String deliveryCondition,
    String deliveryCompany,
    Integer freeShippingConditions,
    Integer deliveryFee,
    List<ProductImgCommand> productImgs,
    List<UpdateProductCommand> products,
    BoardDetailCommand boardDetail,
    ProductInfoNoticeCommand productInfoNotice
) {
}
