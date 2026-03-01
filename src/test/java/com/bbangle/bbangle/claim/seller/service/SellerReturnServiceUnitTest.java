package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
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

@DisplayName("[단위 테스트] SellerReturnService")
@ExtendWith(MockitoExtension.class)
class SellerReturnServiceUnitTest {

    @InjectMocks
    private SellerReturnService sut;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("decision()")
    class DecisionTests {

        @Test
        @DisplayName("반품 승인 - 여러 반품을 한 번에 승인할 수 있다")
        void decision_approve_success() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "승인 사유";

            OrderItem orderItem1 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem2 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem3 = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest1 = ReturnRequestFixture.requested(orderItem1);
            ReturnRequest returnRequest2 = ReturnRequestFixture.requested(orderItem2);
            ReturnRequest returnRequest3 = ReturnRequestFixture.requested(orderItem3);

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(3L);
            given(returnRequestRepository.findAllById(returnIds))
                .willReturn(List.of(returnRequest1, returnRequest2, returnRequest3));

            // when
            sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason);

            // then
            then(returnRequestRepository).should(times(1)).countReturnsBySeller(returnIds, sellerId);
            then(returnRequestRepository).should(times(1)).findAllById(returnIds);
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("반품 거절 - 여러 반품을 한 번에 거절할 수 있다")
        void decision_reject_success() {
            // given
            List<Long> returnIds = List.of(1L, 2L);
            Long sellerId = 10L;
            String reason = "거절 사유";
            OrderItem orderItem1 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem2 = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest1 = ReturnRequestFixture.requested(orderItem1);
            ReturnRequest returnRequest2 = ReturnRequestFixture.requested(orderItem2);

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(2L);
            given(returnRequestRepository.findAllById(returnIds))
                .willReturn(List.of(returnRequest1, returnRequest2));

            // when
            sut.decision(returnIds, sellerId, DecisionType.REJECT, reason);

            // then
            then(returnRequestRepository).should(times(1)).countReturnsBySeller(returnIds, sellerId);
            then(returnRequestRepository).should(times(1)).findAllById(returnIds);
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("판매자 권한 검증 실패 - 판매자가 소유하지 않은 반품은 수정할 수 없다")
        void decision_fails_when_seller_mismatch() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(0L);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(returnRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("부분 일치 실패 - 일부 반품만 판매자 소유일 때 예외가 발생한다")
        void decision_fails_when_partial_match() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L, 4L, 5L);  // 5개 요청
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(3L);  // 3개만 소유

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(returnRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("null 반환 처리 - countReturnsBySeller가 null을 반환하면 예외가 발생한다")
        void decision_fails_when_count_returns_null() {
            // given
            List<Long> returnIds = List.of(1L);
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(null);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }
}
