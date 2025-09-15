package com.bbangle.bbangle.seller.controller;

public class SellerRequest {

    public record sellerCreateRequest(
        String storeName,
        String phoneNumber,
        String subPhoneNumber,
        String email,
        Long verificationNumber,
        String originAddress,
        String originAddressDetail) {

        /*
         * TODO:
         *  1.유효성 검증 로직 추가
         *  2. 서비스 레이어로 값을 전달할 command 객체 생성 로직 필요
         *
         */

    }

}
