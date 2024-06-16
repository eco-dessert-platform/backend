package com.bbangle.bbangle.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDto {

    private Long id;
    private String profile;
    private String title;
    private Integer price;
    private String purchaseUrl;
    private Boolean status;
    private Integer deliveryFee;
    private Integer freeShippingConditions;

    public static BoardDto from(BoardAndImageDto boardAndImage) {
        return BoardDto.builder()
            .id(boardAndImage.id())
            .profile(boardAndImage.profile())
            .title(boardAndImage.title())
            .price(boardAndImage.price())
            .purchaseUrl(boardAndImage.purchaseUrl())
            .status(boardAndImage.status())
            .deliveryFee(boardAndImage.deliveryFee())
            .freeShippingConditions(boardAndImage.freeShippingConditions())
            .build();
    }
}
