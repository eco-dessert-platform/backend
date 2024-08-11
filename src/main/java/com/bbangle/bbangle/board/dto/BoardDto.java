package com.bbangle.bbangle.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDto {

    private Long boardId;
    private Long storeId;
    private String profile;
    private String title;
    private Integer price;
    private String purchaseUrl;
    private Boolean status;
    private Integer deliveryFee;
    private Integer freeShippingConditions;

    public static BoardDto from(BoardAndImageDto boardAndImage) {
        return BoardDto.builder()
            .boardId(boardAndImage.boardId())
            .storeId(boardAndImage.storeId())
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
