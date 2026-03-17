package com.bbangle.bbangle.store.domain;

import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] StoreNameRequest")
class StoreNameRequestTest {

    @Nested
    @DisplayName("createStoreNameRequest() 테스트")
    class CreateStoreNameRequestTest {

        @Test
        @DisplayName("스토어명 변경 신청에 성공한다.")
        void createStoreNameRequest() {

            // give
            Seller seller = SellerFixture.defaultSeller();
            Store store = StoreFixture.defaultStore();

            // when
            StoreNameRequest storeNameRequest = StoreNameRequest.createStoreNameRequest(store, seller, NEW_STORE_NAME);

            // then
            assertThat(storeNameRequest.getCurrentName()).isEqualTo(store.getName());
            assertThat(storeNameRequest.getNewName()).isEqualTo(NEW_STORE_NAME);
            assertThat(storeNameRequest.getStatus()).isEqualTo(StoreApprovalStatus.PENDING);
            assertThat(storeNameRequest.getRejectCategory()).isNull();
            assertThat(storeNameRequest.getRejectDetail()).isNull();
            assertThat(storeNameRequest.getSeller()).isEqualTo(seller);
            assertThat(storeNameRequest.getStore()).isEqualTo(store);
        }
    }

    @Nested
    @DisplayName("approve() 테스트")
    class ApproveTest {

        @Test
        @DisplayName("스토어명 변경을 승인한다.")
        void success_approve() {

            // given
            Store store = StoreFixture.defaultStore();
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(SellerFixture.defaultSeller(), store);

            // when
            storeNameRequest.approve();

            // then
            assertThat(store.getName()).isEqualTo(storeNameRequest.getNewName());
            assertThat(storeNameRequest.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
        }

        @Test
        @DisplayName("거절된 신청은 승인 실패한다.")
        void fail_approve() {

            // given
            Store store = StoreFixture.defaultStore();
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(
                SellerFixture.defaultSeller(), store, StoreApprovalStatus.REJECT
            );

            // when & then
            assertThatThrownBy(storeNameRequest::approve)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.REQUEST_IS_REJECTED);
                });
        }
    }

    @Nested
    @DisplayName("reject() 테스트")
    class RejectTest {

        @Test
        @DisplayName("스토어명 변경 신청을 거절한다.")
        void success_reject() {

            // given
            Store store = StoreFixture.defaultStore();
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(SellerFixture.defaultSeller(), store);

            // when
            storeNameRequest.reject(StoreNameRejectCategory.ETC, "test");

            // then
            assertThat(storeNameRequest.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(storeNameRequest.getRejectCategory()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(storeNameRequest.getRejectDetail()).isEqualTo("test");
        }

        @Test
        @DisplayName("승인된 신청은 거절에 실패한다.")
        void fail_reject() {

            // given
            Store store = StoreFixture.defaultStore();
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(
                SellerFixture.defaultSeller(), store, StoreApprovalStatus.APPROVE
            );

            // when & then
            assertThatThrownBy(() -> storeNameRequest.reject(
                StoreNameRejectCategory.ETC, "test"))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.REQUEST_IS_APPROVED);
                });
        }
    }
}