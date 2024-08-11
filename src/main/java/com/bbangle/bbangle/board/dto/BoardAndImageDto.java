package com.bbangle.bbangle.board.dto;

public record BoardAndImageDto(
    Long boardId,
    Long storeId,
    String profile,
    String title,
    Integer price,
    String purchaseUrl,
    Boolean status,
    Integer deliveryFee,
    Integer freeShippingConditions,
    String url
) {

}
