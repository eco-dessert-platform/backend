package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@DisplayName("[단위 테스트] Seller")
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

    @ParameterizedTest
    @EnumSource(
        value = CertificationStatus.class,
        names = {"PENDING", "APPROVED"}
    )
    @DisplayName("스토어 등록 신청 가능한 상태일 경우 통과한다.")
    void success_register_store(CertificationStatus status) {

        // give
        Seller seller = SellerFixture.defaultSeller(status);

        // when & then
        assertDoesNotThrow(seller::isRegisterAvailable);
    }

    @ParameterizedTest
    @EnumSource(
        value = CertificationStatus.class,
        mode = Mode.EXCLUDE,
        names = {"PENDING", "APPROVED"}
    )
    @DisplayName("스토어 등록 신청 불가능한 상태일 경우 예외가 발생한다.")
    void fail_register_store(CertificationStatus status) {

        // give
        Seller seller = SellerFixture.defaultSeller(status);

        // when & then
        BbangleException exception = assertThrows(BbangleException.class, seller::isRegisterAvailable);
        assertEquals(BbangleErrorCode.ALREADY_REGISTER_STORE, exception.getBbangleErrorCode());
    }
}
