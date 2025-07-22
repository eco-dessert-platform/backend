package com.bbangle.bbangle.board.dto;

public record BoardAndImageDto(
    Long boardId,
    Long storeId,
    String profile,
    Integer imgOrder,
    String title,
    Integer price,
    String purchaseUrl,
    Boolean status,
    Integer deliveryFee,
    Integer freeShippingConditions,
    int discountRate
) {
    public boolean isWithThumbNailImage() {
        return imgOrder == 0;
    }
}
