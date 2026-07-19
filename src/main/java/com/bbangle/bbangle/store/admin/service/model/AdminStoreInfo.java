package com.bbangle.bbangle.store.admin.service.model;

import lombok.Builder;

@Builder
public record AdminStoreInfo(
    String storeName,
    String profile,
    String introduce,
    String identifier,
    String phoneNumber,
    String subPhoneNumber,
    String email,
    String address,
    String addressDetail
) {
}
