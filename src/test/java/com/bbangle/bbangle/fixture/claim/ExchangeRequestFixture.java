package com.bbangle.bbangle.fixture.claim;

import com.bbangle.bbangle.claim.domain.ExchangeRequest;
import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
import com.bbangle.bbangle.order.domain.OrderItem;
import org.springframework.test.util.ReflectionTestUtils;

public class ExchangeRequestFixture {

    private ExchangeRequestFixture() {
    }

    public static ExchangeRequest withStatus(ExchangeRequestStatus status, OrderItem orderItem) {
        return ExchangeRequest.builder()
            .orderItem(orderItem)
            .detailReason("테스트 사유")
            .decidedAt(null)
            .status(status)
            .build();
    }

    public static ExchangeRequest requested(OrderItem orderItem) {
        return withStatus(ExchangeRequestStatus.REQUESTED, orderItem);
    }

    public static ExchangeRequest approved(OrderItem orderItem) {
        return withStatus(ExchangeRequestStatus.APPROVED, orderItem);
    }

    public static ExchangeRequest rejected(OrderItem orderItem) {
        return withStatus(ExchangeRequestStatus.REJECTED, orderItem);
    }

    public static ExchangeRequest withId(ExchangeRequest er, Long id) {
        ReflectionTestUtils.setField(er, "id", id);
        return er;
    }
}
