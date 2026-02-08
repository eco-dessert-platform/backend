package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;

public final class SellerFixture {

    private SellerFixture() {
    }

    public static Seller createDefaultSeller(Store store) {
        return Seller.create(
            "01012345678",
            "02123456789",
            "seller@example.com",
            "서울시 강남구",
            "테스트 빌딩 123호",
            "테스트 판매자 프로필",
            CertificationStatus.APPROVED,
            store
        );
    }

    public static Seller createSellerWithPhone(String phone, String subPhone, Store store) {
        return Seller.create(
            phone,
            subPhone,
            "seller@example.com",
            "서울시 강남구",
            "테스트 빌딩 123호",
            "테스트 판매자 프로필",
            CertificationStatus.APPROVED,
            store
        );
    }

    public static Seller createSellerWithEmail(String email, Store store) {
        return Seller.create(
            "01012345678",
            "02123456789",
            email,
            "서울시 강남구",
            "테스트 빌딩 123호",
            "테스트 판매자 프로필",
            CertificationStatus.APPROVED,
            store
        );
    }

    public static Seller createSellerWithAddress(String addressLine, String detailAddress, Store store) {
        return Seller.create(
            "01012345678",
            "02123456789",
            "seller@example.com",
            addressLine,
            detailAddress,
            "테스트 판매자 프로필",
            CertificationStatus.APPROVED,
            store
        );
    }

    public static Seller createSellerWithStatus(CertificationStatus status, Store store) {
        return Seller.create(
            "01012345678",
            "02123456789",
            "seller@example.com",
            "서울시 강남구",
            "테스트 빌딩 123호",
            "테스트 판매자 프로필",
            status,
            store
        );
    }

}
