package com.bbangle.bbangle.fixture.claim;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.order.domain.OrderItem;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class ReturnRequestFixture {

    private ReturnRequestFixture() {
    }

    public static ReturnRequest withStatus(ReturnRequestRequestStatus status, OrderItem orderItem) {
        return ReturnRequest.builder()
            .orderItem(orderItem)              // 부모(Claim) 필드
            .detailReason("테스트 사유")        // 부모(Claim) 필드(필요 없으면 null 가능)
            .decidedAt(null)                   // 부모(Claim) 필드(결정 전이면 null)
            .status(status)                    // 자식(ReturnRequest) 필드
            .build();
    }

    public static ReturnRequest requested(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.REQUESTED, orderItem);
    }

    public static ReturnRequest approved(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.APPROVED, orderItem);
    }

    public static ReturnRequest rejected(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.REJECTED, orderItem);
    }

    public static ReturnRequest pickupScheduled(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.PICKUP_SCHEDULED, orderItem);
    }

    public static ReturnRequest inspectionApproved(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.INSPECTION_APPROVED, orderItem);
    }

    public static ReturnRequest completed(OrderItem orderItem) {
        return withStatus(ReturnRequestRequestStatus.COMPLETED, orderItem);
    }

    public static ReturnRequest withId(ReturnRequest rr, Long id) {
        ReflectionTestUtils.setField(rr, "id", id); // Claim의 id 필드
        return rr;
    }

    public static ReturnRequest withDecidedAt(ReturnRequest rr, LocalDateTime decidedAt) {
        ReflectionTestUtils.setField(rr, "decidedAt", decidedAt); // Claim 필드
        return rr;
    }

}
