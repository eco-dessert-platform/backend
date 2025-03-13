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

    public static BoardImageDetailResponse of(
            BoardAndImageResponses boardResponses,
            Boolean isWished,
            String boardDetail
    ) {

        return BoardImageDetailResponse.builder()
                .boardId(boardResponses.boardId())
                .storeId(boardResponses.storeId())
                .profile(boardResponses.profile())
                .title(boardResponses.title())
                .price(boardResponses.price())
                .purchaseUrl(boardResponses.purchaseUrl())
                .status(boardResponses.status())
                .deliveryFee(boardResponses.deliveryFee())
                .freeShippingConditions(boardResponses.freeShippingConditions())
                .discountRate(boardResponses.discountRate())
                .isWished(isWished)
                .boardImages(boardResponses.boardImagesWithoutThumbnail())
                .boardDetail(boardDetail)
                .build();
    }
}
