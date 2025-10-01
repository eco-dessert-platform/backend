package com.bbangle.bbangle.seller.store.board.controller.dto;

public class ProductBoardResponse_v3 {

    public record ProductBoardSearchResponse(
        Long storeId,
        String imgUrl,
        String productName,
        String inventoryStatus,
        Long price,
        Long discountPrice,
        Integer fee,
        Integer freeMinPrice,
        SaleStatus saleStatus
    ) {

    }

    private enum SaleStatus {
        ON_SALE("판매중"),
        OUT_OF_STOCK("품절"),
        STOPPED("판매중지"),
        PENDING("판매대기"),
        OTHER("기타");

        private final String description;

        SaleStatus(String description) {
            this.description = description;
        }
    }

}
