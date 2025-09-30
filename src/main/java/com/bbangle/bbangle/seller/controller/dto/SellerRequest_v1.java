package com.bbangle.bbangle.seller.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class SellerRequest_v1 {

    public record SellerDocumentsRegisterRequest(
        @Schema(description = "사업자 등록증", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile businessLicense,

        @Schema(description = "통신판매업 신고증", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile mailOrderLicense,

        @Schema(description = "개인명의 통장 사본", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile bankbookCopy,

        @Schema(description = "즉석식품제조가공업 & 식품제조업", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile foodManufactureLicense
    ) {
    }

}
