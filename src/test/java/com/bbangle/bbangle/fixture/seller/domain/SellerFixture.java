package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import org.springframework.test.util.ReflectionTestUtils;

public final class SellerFixture {

    private SellerFixture() {
    }

    public static Seller defaultSeller() {
        return Seller.create(
            "01012345678",
            "01012345678",
            "test@test.com",
            "서울시 강남구",
            "101호",
            "profile.jpg",
            CertificationStatus.APPROVED,
            StoreFixture.defaultStore()
        );
    }

    public static Seller defaultSeller(Store store) {
        return Seller.create(
            "01012345678",
            "01012345678",
            "test@test.com",
            "서울시 강남구",
            "101호",
            "profile.jpg",
            CertificationStatus.APPROVED,
            store
        );
    }

    public static Seller withId(Seller seller, Long id) {
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }
}
