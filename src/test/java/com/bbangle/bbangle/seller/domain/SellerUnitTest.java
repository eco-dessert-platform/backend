package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
        names = {"NEW", "REJECTED"}
    )
    @DisplayName("스토어 등록 신청 가능한 상태일 경우 통과한다.")
    void success_register_store(CertificationStatus status) {

        // given
        Seller seller = SellerFixture.defaultSeller(status);

        // when & then
        assertDoesNotThrow(seller::validateRegisterAvailable);
    }

    @ParameterizedTest
    @EnumSource(
        value = CertificationStatus.class,
        mode = Mode.EXCLUDE,
        names = {"NEW", "REJECTED"}
    )
    @DisplayName("스토어 등록 신청 불가능한 상태일 경우 예외가 발생한다.")
    void fail_register_store(CertificationStatus status) {

        // given
        Seller seller = SellerFixture.defaultSeller(status);

        // when & then
        BbangleException exception = assertThrows(BbangleException.class, seller::validateRegisterAvailable);
        assertEquals(BbangleErrorCode.ALREADY_REGISTER_STORE, exception.getBbangleErrorCode());
    }

    @Nested
    @DisplayName("registerStore() 테스트")
    class RegisterStoreTest {

        @Test
        @DisplayName("스토어 등록에 성공한다")
        void success_registerStore() {

            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
            Store store = StoreFixture.defaultStore();

            // when
            seller.registerStore(store, "new-name");

            // then
            assertThat(seller.getStore()).isEqualTo(store);
            assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
            assertThat(seller.getName()).isEqualTo("new-name");
        }

        @Test
        @DisplayName("이름이 null이면 기존 이름을 유지한다")
        void success_registerStore_null_name() {

            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
            String originalName = seller.getName();
            Store store = StoreFixture.defaultStore();

            // when
            seller.registerStore(store, null);

            // then
            assertThat(seller.getName()).isEqualTo(originalName);
            assertThat(seller.getStore()).isEqualTo(store);
            assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        }

        @Test
        @DisplayName("이미 스토어가 존재하면 등록에 실패한다")
        void fail_registerStore_when_store_already_exists() {

            // given
            Store newStore = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller(newStore);

            // when & then
            assertThatThrownBy(() -> seller.registerStore(newStore, "name"))
                .isInstanceOfSatisfying(BbangleException.class, ex ->
                    assertThat(ex.getBbangleErrorCode())
                        .isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE)
                );

            assertThat(seller.getStore()).isEqualTo(newStore);
        }

        @Test
        @DisplayName("이미 승인된 상태이면 등록에 실패한다")
        void fail_registerStore_when_already_approved() {

            // given
            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller("test", store);
            Store newStore = StoreFixture.defaultStore();

            // when & then
            assertThatThrownBy(() -> seller.registerStore(newStore, "name"))
                .isInstanceOfSatisfying(BbangleException.class, ex ->
                    assertThat(ex.getBbangleErrorCode())
                        .isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE)
                );

            assertThat(seller.getStore()).isNotEqualTo(newStore);
        }

        @Test
        @DisplayName("스토어가 null이면 등록에 실패한다")
        void fail_registerStore_when_store_is_null() {

            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);

            // when & then
            assertThatThrownBy(() -> seller.registerStore(null, "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> {
                    IllegalArgumentException ex = (IllegalArgumentException) e;
                    assertThat(ex.getMessage()).isEqualTo("store must not be null");
                });
        }
    }
}
