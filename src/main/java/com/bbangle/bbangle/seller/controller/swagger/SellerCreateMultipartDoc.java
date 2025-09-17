package com.bbangle.bbangle.seller.controller.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record SellerCreateMultipartDoc(
    @Schema(description = "스토어명", example = "빵그리의 오븐 1호점")
    String storeName,

    @Schema(description = "연락처", example = "01012345678")
    String phoneNumber,

    @Schema(description = "서브 연락처", example = "01012345678")
    String subPhoneNumber,

    @Schema(description = "이메일", example = "user@example.com", format = "email")
    String email,

    @Schema(description = "인증번호(6자리)", example = "123456")
    Long verificationNumber,

    @Schema(description = "판매자 주소", example = "(우편번호) 성남시 금광동 222-31")
    String originAddress,

    @Schema(description = "판매자 상세 주소", example = "나동 202호")
    String originAddressDetail,
    
    @Schema(description = "프로필 이미지 파일", type = "string", format = "binary")
    MultipartFile profileImage
) {


}
