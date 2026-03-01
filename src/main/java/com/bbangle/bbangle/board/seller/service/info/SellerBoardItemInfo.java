package com.bbangle.bbangle.board.seller.service.info;

import com.bbangle.bbangle.board.domain.DeliveryType;
import com.bbangle.bbangle.board.domain.InventoryStatus;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.repository.dao.SellerBoardDao;

public record SellerBoardItemInfo(
    Long boardId,
    String thumbnailUrl,
    String title,
    InventoryStatus inventoryStatus,
    int price,
    int discountPrice,
    int discountValue,
    Integer deliveryFee,
    Integer freeShippingConditions,
    DeliveryType deliveryType,
    SaleStatus saleStatus
) {

    public static SellerBoardItemInfo of(SellerBoardDao dao, InventoryStatus inventoryStatus) {
        return new SellerBoardItemInfo(
            dao.boardId(),
            dao.thumbnailUrl(),
            dao.title(),
            inventoryStatus,
            dao.price(),
            dao.discountPrice(),
            dao.discountValue(),
            dao.deliveryFee(),
            dao.freeShippingConditions(),
            DeliveryType.from(dao.deliveryFee(), dao.freeShippingConditions()),
            dao.saleStatus()
        );
    }
}
