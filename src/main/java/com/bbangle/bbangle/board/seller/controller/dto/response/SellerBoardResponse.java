package com.bbangle.bbangle.board.seller.controller.dto.response;

import com.bbangle.bbangle.board.domain.DeliveryType;
import com.bbangle.bbangle.board.domain.InventoryStatus;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardItemInfo;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import java.util.Map;

public class SellerBoardResponse {

    public record SellerBoardListResponse(
        Map<SaleStatus, Long> tabCounts,
        BbanglePageResponse<SellerBoardSearchResponse> boards
    ) {

    }

    public record SellerBoardSearchResponse(
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

        public static SellerBoardSearchResponse from(SellerBoardItemInfo info) {
            return new SellerBoardSearchResponse(
                info.boardId(),
                info.thumbnailUrl(),
                info.title(),
                info.inventoryStatus(),
                info.price(),
                info.discountPrice(),
                info.discountValue(),
                info.deliveryFee(),
                info.freeShippingConditions(),
                info.deliveryType(),
                info.saleStatus()
            );
        }
    }
}
