package com.bbangle.bbangle.store.domain;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("[단위 테스트] StoreApplication")
class StoreApplicationTest {

    @Test
    @DisplayName("스토어 등록 신청 객체 생성에 성공한다.")
    void success_create_storeApplication() {

        // given & when
        Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), 1L);
        Store store = StoreFixture.defaultStore();
        StoreApplication storeApplication = StoreApplication.createStoreApplication(
            DEFAULT_STORE_NAME, DEFAULT_PROFILE,
            DEFAULT_INTRODUCE,
            DEFAULT_PHONE, DEFAULT_SUBPHONE,
            DEFAULT_EMAIL,
            DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS,
            seller, store
        );

        // then
        assertThat(storeApplication).isNotNull();
        assertThat(storeApplication.getName()).isEqualTo(DEFAULT_STORE_NAME);
        assertThat(storeApplication.getProfile()).isEqualTo(DEFAULT_PROFILE);
        assertThat(storeApplication.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
        assertThat(storeApplication.getStatus()).isEqualTo(StoreApprovalStatus.PENDING);
        assertThat(storeApplication.getPhoneNumberVO().getPhoneNumber()).isEqualTo(DEFAULT_PHONE);
        assertThat(storeApplication.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(DEFAULT_SUBPHONE);
        assertThat(storeApplication.getEmailVO().getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(storeApplication.getOriginAddressLine()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(storeApplication.getOriginAddressDetail()).isEqualTo(DEFAULT_DETAIL_ADDRESS);
        assertThat(storeApplication.getSeller()).isEqualTo(seller);
        assertThat(storeApplication.getStore()).isEqualTo(store);
    }

    @Nested
    @DisplayName("reject() 테스트")
    class RejectTest {

        @Test
        @DisplayName("승인 대기 중인 요청을 거절한다.")
        void success_reject() {
            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(seller, null);

            // when
            application.reject();

            // then
            assertThat(application.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.REJECTED);
        }

        @Test
        @DisplayName("이미 승인된 판매자의 승인 대기중인 요청을 거절할 경우 해당 요청만 거절된 상태로 업데이트된다.")
        void reject_already_approved_seller() {
            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.APPROVED);
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(seller, null);

            // when
            application.reject();

            // then
            assertThat(application.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        }

        @Test
        @DisplayName("이미 승인된 요청을 거절할 경우 예외가 발생한다.")
        void fail_reject_already_approved_application() {
            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.APPROVED);
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(DEFAULT_STORE_NAME, seller, StoreApprovalStatus.APPROVE);

            // when & then
            assertThatThrownBy(application::reject)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.REQUEST_IS_APPROVED);
                });
        }
    }

    @Nested
    @DisplayName("validateApprovable() 테스트")
    class ValidateApprovableTest {

        @ParameterizedTest
        @DisplayName("승인 불가능 상태에서는 예외가 발생한다.")
        @EnumSource(value = StoreApprovalStatus.class, names = {"APPROVE", "REJECT"})
        void validateApprovable_notApprovable(StoreApprovalStatus status) {

            // given
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(DEFAULT_STORE_NAME, seller, status);

            // when & then
            assertThatThrownBy(application::validateApprovable)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    BbangleErrorCode expected = (status == StoreApprovalStatus.APPROVE)
                        ? BbangleErrorCode.REQUEST_IS_APPROVED
                        : BbangleErrorCode.REQUEST_IS_REJECTED;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(expected);
                });
        }

        @Test
        @DisplayName("승인 가능한 상태에서는 예외가 발생하지 않는다.")
        void validateApprovable_approvable() {

            // given
            Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(DEFAULT_STORE_NAME, seller, StoreApprovalStatus.PENDING);

            // when & then
            assertThatCode(application::validateApprovable).doesNotThrowAnyException();
        }
    }
}