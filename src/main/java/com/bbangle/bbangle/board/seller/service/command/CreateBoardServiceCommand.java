package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateBoardServiceCommand(
    Store store,
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
    List<ProductCommand> products,
    BoardDetailCommand boardDetail,
    ProductInfoNoticeCommand productInfoNotice
) {
}
