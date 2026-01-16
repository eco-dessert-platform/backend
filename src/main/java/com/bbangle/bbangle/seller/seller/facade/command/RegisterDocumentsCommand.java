package com.bbangle.bbangle.seller.seller.facade.command;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

public record RegisterDocumentsCommand(
    MultipartFile businessLicense,

    MultipartFile mailOrderLicense,

    MultipartFile bankbookCopy,

    MultipartFile foodManufactureLicense,

    Long accountVerificationId,

    Long sellerId
) {
    @Builder
    public RegisterDocumentsCommand(MultipartFile businessLicense, MultipartFile mailOrderLicense,
                                    MultipartFile bankbookCopy, MultipartFile foodManufactureLicense,
                                    Long accountVerificationId, Long sellerId) {
        this.businessLicense = businessLicense;
        this.mailOrderLicense = mailOrderLicense;
        this.bankbookCopy = bankbookCopy;
        this.foodManufactureLicense = foodManufactureLicense;
        this.accountVerificationId = accountVerificationId;
        this.sellerId = sellerId;
    }
}
