package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public final class SellerFixture {

    private SellerFixture() {
    }

    public static Seller defaultSeller() {
        return Seller.create(
            "testSeller",
            OauthServerType.KAKAO,
            "providerId"
        );
    }

    public static Seller withId(Seller seller, Long id) {
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }

    public static Seller defaultSeller(CertificationStatus status) {
        return Seller.builder()
            .name("test")
            .provider(OauthServerType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .certificationStatus(status)
            .isDeleted(false)
            .store(null)
            .build();
    }
}
