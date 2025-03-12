package com.bbangle.bbangle.board.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardImageDetailResponse {

    private Long boardId;
    private Long storeId;
    private String profile;
    private String title;
    private Integer price;
    private String purchaseUrl;
    private Boolean status;
    private Integer deliveryFee;
    private Integer freeShippingConditions;
    private int discountRate;
    private Boolean isWished;
    private List<String> boardImages;
    private String boardDetail;

    public static BoardImageDetailResponse from(
        BoardDto boardDto,
        Boolean isWished,
        String boardProfileUrl,
        List<String> boardImageDtos,
        String boardDetai
    ) {
        return BoardImageDetailResponse.builder()
            .boardId(boardDto.getBoardId())
            .storeId(boardDto.getStoreId())
            .profile(boardProfileUrl)
            .title(boardDto.getTitle())
            .price(boardDto.getPrice())
            .purchaseUrl(boardDto.getPurchaseUrl())
            .status(boardDto.getStatus())
            .deliveryFee(boardDto.getDeliveryFee())
            .freeShippingConditions(boardDto.getFreeShippingConditions())
            .discountRate(boardDto.getDiscountRate())
            .isWished(isWished)
            .boardImages(boardImageDtos)
            .boardDetail(boardDetai)
            .build();
    }
}
