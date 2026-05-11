package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;

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

        PhoneNumberVO phoneNumberVO = store.getPhoneNumberVO();
        EmailVO emailVO = store.getEmailVO();
        return new RegisteredStoreInfo(
            store.getId(),
            store.getName(),
            store.getIdentifier(),
            seller != null ? seller.getName() : null,
            phoneNumberVO != null ? phoneNumberVO.getPhoneNumber() : null,
            phoneNumberVO != null ? phoneNumberVO.getSubPhoneNumber() : null,
            emailVO != null ? emailVO.getEmail() : null,
            address
        );
    }
}
