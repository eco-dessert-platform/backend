package com.bbangle.bbangle.seller.seller.facade.command;

import org.springframework.web.multipart.MultipartFile;

public record RegisterDocumentsCommand(
    MultipartFile businessLicense,

    MultipartFile mailOrderLicense,

    MultipartFile bankbookCopy,

    MultipartFile foodManufactureLicense,

    Long accountVerificationId,

    Long sellerId
) {
}
