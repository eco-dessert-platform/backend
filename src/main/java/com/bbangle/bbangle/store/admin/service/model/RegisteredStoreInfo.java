package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;

public record RegisteredStoreInfo(
    long storeId,
    String storeName,
    String businessNumber,
    String introduce,
    String phoneNumber,
    String subPhoneNumber,
    String email,
    String originAddressLine,
    String originAddressDetail
) {

    public static RegisteredStoreInfo from(Store store) {
        PhoneNumberVO phoneNumberVO = store.getPhoneNumberVO();
        EmailVO emailVO = store.getEmailVO();
        return new RegisteredStoreInfo(
            store.getId(),
            store.getName(),
            store.getIdentifier(),
            store.getIntroduce(),
            phoneNumberVO != null ? phoneNumberVO.getPhoneNumber() : null,
            phoneNumberVO != null ? phoneNumberVO.getSubPhoneNumber() : null,
            emailVO != null ? emailVO.getEmail() : null,
            store.getOriginAddressLine(),
            store.getOriginAddressDetail()
        );
    }
}
