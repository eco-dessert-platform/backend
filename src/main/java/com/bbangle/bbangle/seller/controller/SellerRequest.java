package com.bbangle.bbangle.seller.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class SellerRequest {

    public record SellerCreateRequest(
        @NotBlank
        String storeName,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String subPhoneNumber,

        @Email
        String email,

        @NotNull
        Long verificationNumber,

        @NotBlank
        String originAddress,

        @NotBlank
        String originAddressDetail,

        MultipartFile profileImage

    ) {
        /*
         * TODO:
         *  1.유효성 검증 로직 추가
         *  2. 서비스 레이어로 값을 전달할 command 객체 생성 로직 필요
         */
    }

}
