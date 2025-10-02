package com.bbangle.bbangle.seller.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SellerRequest_v3 {

    public record SellerCreateRequest(
        @Schema(description = "스토어명", example = "빵그리의 오븐 1호점")
        @NotBlank
        String storeName,

        @Schema(description = "연락처", example = "01012345678")
        @NotBlank
        String phoneNumber,

        @Schema(description = "서브 연락처", example = "01012345678")
        @NotBlank
        String subPhoneNumber,

        @Schema(description = "이메일", example = "user@example.com", format = "email")
        @Email
        String email,

        @Schema(description = "인증번호(6자리)", example = "123456")
        @NotNull
        Long verificationNumber,

        @Schema(description = "판매자 주소", example = "(우편번호) 성남시 금광동 222-31")
        @NotBlank
        String originAddress,

        @Schema(description = "판매자 상세 주소", example = "나동 202호")
        @NotBlank
        String originAddressDetail
    ) {
        /*
         * TODO:
         *  1.유효성 검증 로직 추가
         *  2. 서비스 레이어로 값을 전달할 command 객체 생성 로직 필요
         */
    }

}
