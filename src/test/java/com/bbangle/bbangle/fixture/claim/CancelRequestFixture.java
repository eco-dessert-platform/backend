package com.bbangle.bbangle.fixture.claim;

import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus;
import com.bbangle.bbangle.order.domain.OrderItem;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class CancelRequestFixture {

    private CancelRequestFixture() {
    }

    public static CancelRequest withStatus(CancelRequestStatus status, OrderItem orderItem) {
        return CancelRequest.builder()
            .orderItem(orderItem)              // 부모(Claim) 필드
            .detailReason("테스트 사유")        // 부모(Claim) 필드(필요 없으면 null 가능)
            .decidedAt(null)                   // 부모(Claim) 필드(결정 전이면 null)
            .status(status)                    // 자식(CancelRequest) 필드
            .build();
    }

    public static CancelRequest requested(OrderItem orderItem) {
        return withStatus(CancelRequestStatus.REQUESTED, orderItem);
    }

    public static CancelRequest approved(OrderItem orderItem) {
        return withStatus(CancelRequestStatus.APPROVED, orderItem);
    }

    public static CancelRequest rejected(OrderItem orderItem) {
        return withStatus(CancelRequestStatus.REJECTED, orderItem);
    }

    public static CancelRequest withId(CancelRequest cr, Long id) {
        ReflectionTestUtils.setField(cr, "id", id); // Claim의 id 필드
        return cr;
    }

    public static CancelRequest withDecidedAt(CancelRequest cr, LocalDateTime decidedAt) {
        ReflectionTestUtils.setField(cr, "decidedAt", decidedAt); // Claim 필드
        return cr;
    }

}
