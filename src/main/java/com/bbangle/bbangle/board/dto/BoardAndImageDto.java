package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BoardAndImageDto(
    Long id,
    String profile,
    String title,
    Integer price,
    String purchaseUrl,
    Boolean status,
    Integer deliveryFee,
    Integer freeShippingConditions,
    String url
) {

    @QueryProjection
    public BoardAndImageDto(
        Long id,
        String profile,
        String title,
        Integer price,
        String purchaseUrl,
        Boolean status,
        Integer deliveryFee,
        Integer freeShippingConditions,
        String url
    ) {
        this.id = id;
        this.profile = profile;
        this.title = title;
        this.price = price;
        this.purchaseUrl = purchaseUrl;
        this.status = status;
        this.deliveryFee = deliveryFee;
        this.freeShippingConditions = freeShippingConditions;
        this.url = url;
    }
}
