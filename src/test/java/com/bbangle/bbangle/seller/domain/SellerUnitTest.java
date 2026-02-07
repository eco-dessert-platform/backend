package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] Seller")
@ExtendWith(MockitoExtension.class)
public class SellerUnitTest {

    @Test
    @DisplayName("판매자 정보 생성에 성공한다.")
    void success_create_Seller() {

        // given
        String name = "seller";
        OauthServerType provider = OauthServerType.KAKAO;
        String providerId = "12345";

        // when
        Seller seller = Seller.create(name, provider, providerId);

        // then
        assertThat(seller).isNotNull();
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getProvider()).isEqualTo(provider);
        assertThat(seller.getProviderId()).isEqualTo(providerId);

        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
        assertThat(seller.isDeleted()).isFalse();
        assertThat(seller.getStore()).isNull();
    }

    @Test
    @DisplayName("스토어 등록 시 스토어 연결 + 상태 변경 + 인증 상태 변경")
    void success_registerStore() {

        // given
        String name = "seller";
        OauthServerType provider = OauthServerType.KAKAO;
        String providerId = "12345";
        Seller seller = Seller.create(name, provider, providerId);

        Store store = Store.createForSeller(
            "빵그리",
            "test.com",
            "introduce",
            "01012345678",
            "01098765432",
            "123@test.com",
            "서울",
            "123동"
        );

        // when
        seller.registerStore(store);

        // then
        assertThat(seller.getStore()).isEqualTo(store);
        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(seller.getStore().getStatus()).isEqualTo(StoreStatus.RESERVED);
    }
}
