package com.bbangle.bbangle.seller.admin.controller.dto;

import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerStoreInfo;
import java.util.List;
import lombok.Builder;

public class AdminSellerResponse {

    @Builder
    public record AdminSellerApplication(
        Long storeApplicationId,
        SellerStoreInfo sellerStoreInfo,
        SellerInfo sellerInfo
    ) {}

    @Builder
    public record AdminSellerApplicationList(
        List<AdminSellerApplication> adminSellerApplicationList,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
    ) {}
}
