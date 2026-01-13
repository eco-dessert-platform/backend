package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] OAuth2 Seller")
class OAuth2SellerUnitTest {

    @Test
    @DisplayName("판매자 정보 생성에 성공한다.")
    void success_create_OAuth2Seller() {

        // given
        String name = "seller";
        OauthServerType provider = OauthServerType.KAKAO;
        String providerId = "12345";

        // when
        OAuth2Seller seller = OAuth2Seller.create(name, provider, providerId);

        // then
        assertThat(seller).isNotNull();
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getProvider()).isEqualTo(provider);
        assertThat(seller.getProviderId()).isEqualTo(providerId);

        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
        assertThat(seller.isDeleted()).isFalse();
        assertThat(seller.getStore()).isNull();
    }
}