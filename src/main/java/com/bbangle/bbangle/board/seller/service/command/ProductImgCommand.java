package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.board.domain.ProductImg;

public record ProductImgCommand(
    String url,
    int imgOrder
) {

    public ProductImg toProductImg() {
        return ProductImg.builder()
            .url(url)
            .imgOrder(imgOrder)
            .build();
    }
}
