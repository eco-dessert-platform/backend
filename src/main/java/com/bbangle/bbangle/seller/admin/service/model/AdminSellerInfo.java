package com.bbangle.bbangle.seller.admin.service.model;

import java.time.LocalDateTime;
import lombok.Builder;

public class AdminSellerInfo {

    @Builder
    public record SellerStoreInfo(
        String storeName,
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail
    ) {}

    @Builder
    public record SellerInfo(
        Long sellerId,
        String bankCode,
        String accountHolder,
        String accountNumber,
        LocalDateTime createdAt
    ) {}
}
