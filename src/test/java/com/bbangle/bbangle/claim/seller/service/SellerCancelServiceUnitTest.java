package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.CancelRequestRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.CancelRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] SellerCancelService")
@ExtendWith(MockitoExtension.class)
class SellerCancelServiceUnitTest {

    @InjectMocks
    private SellerCancelService sut;

    @Mock
    private CancelRequestRepository cancelRequestRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("decision()")
    class DecisionTests {

        @Test
        @DisplayName("취소 승인 - 여러 취소를 한 번에 승인할 수 있다")
        void decision_approve_success() {
            // given
            List<Long> cancelIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "승인 사유";

            OrderItem orderItem1 = OrderItemFixture.orderCancelRequested();
            OrderItem orderItem2 = OrderItemFixture.orderCancelRequested();
            OrderItem orderItem3 = OrderItemFixture.orderCancelRequested();
            CancelRequest cancelRequest1 = CancelRequestFixture.requested(orderItem1);
            CancelRequest cancelRequest2 = CancelRequestFixture.requested(orderItem2);
            CancelRequest cancelRequest3 = CancelRequestFixture.requested(orderItem3);

            given(cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId)).willReturn(3L);
            given(cancelRequestRepository.findAllById(cancelIds))
                .willReturn(List.of(cancelRequest1, cancelRequest2, cancelRequest3));

            // when
            sut.decision(cancelIds, sellerId, DecisionType.APPROVE, reason);

            // then
            then(cancelRequestRepository).should(times(1)).countCancelsBySeller(cancelIds, sellerId);
            then(cancelRequestRepository).should(times(1)).findAllById(cancelIds);
            then(orderItemHistoryRepository).should(times(3)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("취소 거절 - 여러 취소를 한 번에 거절할 수 있다")
        void decision_reject_success() {
            // given
            List<Long> cancelIds = List.of(1L, 2L);
            Long sellerId = 10L;
            String reason = "거절 사유";

            OrderItem orderItem1 = OrderItemFixture.orderCancelRequested();
            OrderItem orderItem2 = OrderItemFixture.orderCancelRequested();
            CancelRequest cancelRequest1 = CancelRequestFixture.requested(orderItem1);
            CancelRequest cancelRequest2 = CancelRequestFixture.requested(orderItem2);

            given(cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId)).willReturn(2L);
            given(cancelRequestRepository.findAllById(cancelIds))
                .willReturn(List.of(cancelRequest1, cancelRequest2));

            // when
            sut.decision(cancelIds, sellerId, DecisionType.REJECT, reason);

            // then
            then(cancelRequestRepository).should(times(1)).countCancelsBySeller(cancelIds, sellerId);
            then(cancelRequestRepository).should(times(1)).findAllById(cancelIds);
            then(orderItemHistoryRepository).should(times(2)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("판매자 권한 검증 실패 - 판매자가 소유하지 않은 취소는 수정할 수 없다")
        void decision_fails_when_seller_mismatch() {
            // given
            List<Long> cancelIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "사유";

            given(cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId)).willReturn(0L);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(cancelIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(cancelRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("부분 일치 실패 - 일부 취소만 판매자 소유일 때 예외가 발생한다")
        void decision_fails_when_partial_match() {
            // given
            List<Long> cancelIds = List.of(1L, 2L, 3L, 4L, 5L);  // 5개 요청
            Long sellerId = 10L;
            String reason = "사유";

            given(cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId)).willReturn(3L);  // 3개만 소유

            // when & then
            assertThatThrownBy(
                () -> sut.decision(cancelIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(cancelRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("null 반환 처리 - countCancelsBySeller가 null을 반환하면 예외가 발생한다")
        void decision_fails_when_count_returns_null() {
            // given
            List<Long> cancelIds = List.of(1L);
            Long sellerId = 10L;
            String reason = "사유";

            given(cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId)).willReturn(null);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(cancelIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }
}
