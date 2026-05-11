package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;

public record RegisteredStoreInfo(
    long storeId,
    String storeName,
    String businessNumber,
    String sellerName,
    String phoneNumber,
    String subPhoneNumber,
    String email,
    String address
) {

    public static RegisteredStoreInfo from(Store store, Seller seller) {
        String detail = store.getOriginAddressDetail();
        String address = (detail != null && !detail.isBlank())
            ? store.getOriginAddressLine() + " " + detail
            : store.getOriginAddressLine();

        return new RegisteredStoreInfo(
            store.getId(),
            store.getName(),
            store.getIdentifier(),
            seller != null ? seller.getName() : null,
            store.getPhoneNumberVO().getPhoneNumber(),
            store.getPhoneNumberVO().getSubPhoneNumber(),
            store.getEmailVO().getEmail(),
            address
        );
    }
}
