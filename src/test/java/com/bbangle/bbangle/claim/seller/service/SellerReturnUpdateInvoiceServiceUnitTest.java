package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ClaimDelivery;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.ClaimDeliveryType;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ClaimDeliveryRepository;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.claim.seller.controller.dto.UpdateReturnInvoiceResponse;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] SellerReturnService.updateReturnInvoice()")
@ExtendWith(MockitoExtension.class)
class SellerReturnUpdateInvoiceServiceUnitTest {

    @InjectMocks
    private SellerReturnService sut;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private ClaimDeliveryRepository claimDeliveryRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    private static final Long RETURN_ID = 1L;
    private static final Long SELLER_ID = 10L;
    private static final CourierCompany NEW_COURIER = CourierCompany.LOTTE;
    private static final String NEW_TRACKING_NUMBER = "9876543210";

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("PICKUP_SCHEDULED 상태에서 운송장을 수정하면 갱신된 택배사와 운송장 번호가 반환된다")
        void updateReturnInvoice_success_when_pickup_scheduled() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.pickupScheduled(orderItem);

            ClaimDelivery mockDelivery = mock(ClaimDelivery.class);
            Shipping mockShipping = mock(Shipping.class);
            given(mockDelivery.getShipping()).willReturn(mockShipping);
            given(mockShipping.getCourierName()).willReturn(NEW_COURIER.getDisplayName());
            given(mockShipping.getTrackingNumber()).willReturn(NEW_TRACKING_NUMBER);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.of(returnRequest));
            given(claimDeliveryRepository.findByClaimIdAndDeliveryType(RETURN_ID, ClaimDeliveryType.RETURN_PICKUP))
                .willReturn(Optional.of(mockDelivery));

            // when
            UpdateReturnInvoiceResponse response =
                sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER);

            // then
            then(mockDelivery).should(times(1)).updateInvoice(NEW_COURIER, NEW_TRACKING_NUMBER);
            assertThat(response.returnId()).isEqualTo(RETURN_ID);
            assertThat(response.courierName()).isEqualTo(NEW_COURIER.getDisplayName());
            assertThat(response.trackingNumber()).isEqualTo(NEW_TRACKING_NUMBER);
        }
    }

    @Nested
    @DisplayName("실패 케이스 — 권한 및 존재 검증")
    class AuthAndNotFoundFailures {

        @Test
        @DisplayName("판매자 소유권 검증 실패 시 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void updateReturnInvoice_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(returnRequestRepository).should(never()).findById(any());
            then(claimDeliveryRepository).should(never()).findByClaimIdAndDeliveryType(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 returnId로 요청하면 CLAIM_NOT_FOUND 예외가 발생한다")
        void updateReturnInvoice_fails_when_claim_not_found() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_NOT_FOUND);

            then(claimDeliveryRepository).should(never()).findByClaimIdAndDeliveryType(any(), any());
        }

        @Test
        @DisplayName("PICKUP_SCHEDULED이지만 ClaimDelivery 레코드가 없으면 DELIVERY_NOT_FOUND 예외가 발생한다")
        void updateReturnInvoice_fails_when_delivery_not_found() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.pickupScheduled(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.of(returnRequest));
            given(claimDeliveryRepository.findByClaimIdAndDeliveryType(RETURN_ID, ClaimDeliveryType.RETURN_PICKUP))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("실패 케이스 — 상태 검증")
    class StatusValidationFailures {

        @Test
        @DisplayName("PICKED_UP 이후 상태에서 수정을 시도하면 DELIVERY_MODIFY_NOT_ALLOWED 예외가 발생한다")
        void updateReturnInvoice_fails_when_already_picked_up() {
            // given — 수거가 완료된 상태
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.withStatus(
                ReturnRequestRequestStatus.PICKED_UP, orderItem
            );

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);

            then(claimDeliveryRepository).should(never()).findByClaimIdAndDeliveryType(any(), any());
        }

        @Test
        @DisplayName("INSPECTING 상태에서 수정을 시도하면 DELIVERY_MODIFY_NOT_ALLOWED 예외가 발생한다")
        void updateReturnInvoice_fails_when_inspecting() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.withStatus(
                ReturnRequestRequestStatus.INSPECTING, orderItem
            );

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);

            then(claimDeliveryRepository).should(never()).findByClaimIdAndDeliveryType(any(), any());
        }

        @Test
        @DisplayName("APPROVED 상태(수거 등록 전)에서 수정을 시도하면 DELIVERY_MODIFY_NOT_ALLOWED 예외가 발생한다")
        void updateReturnInvoice_fails_when_approved() {
            // given — 아직 운송장이 등록되지 않은 단계
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_APPROVED);
            ReturnRequest returnRequest = ReturnRequestFixture.approved(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.updateReturnInvoice(RETURN_ID, SELLER_ID, NEW_COURIER, NEW_TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);
        }
    }
}
