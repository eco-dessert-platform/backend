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
    private List<String> boardDetails;

    public static BoardImageDetailResponse from(
        BoardDto boardDto,
        Boolean isWished,
        List<String> boardImageDtoList,
        List<String> boardDetailList
    ) {
        return BoardImageDetailResponse.builder()
            .boardId(boardDto.getBoardId())
            .storeId(boardDto.getStoreId())
            .profile(boardDto.getProfile())
            .title(boardDto.getTitle())
            .price(boardDto.getPrice())
            .purchaseUrl(boardDto.getPurchaseUrl())
            .status(boardDto.getStatus())
            .deliveryFee(boardDto.getDeliveryFee())
            .freeShippingConditions(boardDto.getFreeShippingConditions())
            .discountRate(boardDto.getDiscountRate())
            .isWished(isWished)
            .boardImages(boardImageDtoList)
            .boardDetails(boardDetailList)
            .build();
    }
}
