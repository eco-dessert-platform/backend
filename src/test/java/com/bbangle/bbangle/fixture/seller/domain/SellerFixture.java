package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public final class SellerFixture {

    private SellerFixture() {
    }

    public static Seller defaultSeller() {
        return Seller.create(
            "testSeller",
            OauthServerType.KAKAO,
            UUID.randomUUID().toString()
        );
    }

    public static Seller defaultSeller(CertificationStatus status) {
        return Seller.builder()
            .name("test")
            .provider(OauthServerType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .certificationStatus(status)
            .store(null)
            .build();
    }

    public static Seller defaultSeller(Store store) {
        return defaultSeller("test", store);
    }

    public static Seller defaultSeller(String name, Store store) {
        return Seller.builder()
            .name(name)
            .provider(OauthServerType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .certificationStatus(CertificationStatus.APPROVED)
            .store(store)
            .build();
    }

    public static Seller defaultSeller(String name, Store store) {
        return Seller.builder()
            .name(name)
            .provider(OauthServerType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .certificationStatus(CertificationStatus.APPROVED)
            .store(store)
            .build();
    }

    public static Seller withId(Seller seller, Long id) {
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }

    public static Seller createDefaultSeller(Store store) {
        return Seller.builder()
            .name("테스트 판매자")
            .provider(OauthServerType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .certificationStatus(CertificationStatus.APPROVED)
            .store(store)
            .build();
    }
}
