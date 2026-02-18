package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import lombok.Builder;

@Builder
public record ProductInfoNoticeCommand(
    String productName,
    String foodType,
    String manufacturer,
    String originLocation,
    String manufactureDate,
    String expirationDate,
    String storageGuide,
    String packagingQuantityUnit,
    String rawMaterialName,
    String nutritionInfo,
    String transgenic,
    String customerWarning,
    String importFood
) {

    public ProductInfoNotice toProductInfoNotice() {
        return new ProductInfoNotice(
            productName,
            foodType,
            manufacturer,
            originLocation,
            manufactureDate,
            expirationDate,
            storageGuide,
            packagingQuantityUnit,
            rawMaterialName,
            nutritionInfo,
            transgenic,
            customerWarning,
            importFood
        );
    }
}
