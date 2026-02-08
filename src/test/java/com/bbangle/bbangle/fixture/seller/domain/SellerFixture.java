package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.UUID;

public class SellerFixture {

    // dev 브랜치에 defaultSeller를 사용하고 있어서 임시로 설정하였습니다.
    public static Seller defaultSellerMergeConflict() {
        return Seller.create(
            "testSeller",
            OauthServerType.KAKAO,
            "providerId"
        );
    }

    public static Seller defaultSellerMergeConflict(CertificationStatus status) {
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
