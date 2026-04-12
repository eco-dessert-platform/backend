package com.bbangle.bbangle.seller.admin.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminSellerResponse {

    @Builder
    public record AdminSellerApplication(
        Long storeApplicationId,
        SellerStoreDTO sellerStoreDTO,
        SellerDTO sellerDTO
    ) {
        @Builder
        public record SellerStoreDTO(
            String storeName,
            String phone,
            String subPhone,
            String email,
            String originAddressLine,
            String originAddressDetail
        ) {}

        @Builder
        public record SellerDTO(
            Long sellerId,
            String bankCode,
            String accountHolder,
            String accountNumber,
            LocalDateTime createdAt
        ) {}
    }

    @Builder
    public record AdminSellerApplicationList(
        List<AdminSellerApplication> adminSellerApplicationList,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
    ) {}
}
