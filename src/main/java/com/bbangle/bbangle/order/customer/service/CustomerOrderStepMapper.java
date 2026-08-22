package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import java.util.List;

/**
 * (orderStatus, deliveryStatus) → 소비자 화면 진행 단계({@link CustomerOrderProgress}) 변환기.
 *
 * <p>화면 단계는 두 enum 의 조합으로 결정됩니다.
 * <ul>
 *   <li>일반: 결제완료 → 상품제작중 → 상품발송 → 배송완료 → 구매확정</li>
 *   <li>반품: 반품신청 → 반품승인 → 상품회수중 → 상품확인중 → 반품완료 (거절/반려/보류는 분기)</li>
 *   <li>교환: 교환신청 → 교환승인 → 상품회수중 → 상품확인중 → 교환진행 → 상품발송 → 배송완료 → 교환완료
 *       (거절/반려/보류는 분기)</li>
 *   <li>취소: 취소요청 → 취소완료 (거절은 분기)</li>
 * </ul>
 *
 * <p>"배송완료"는 OrderStatus 에 없는 값으로, 상품발송 상태이면서 배송상태가 {@code DELIVERED} 일 때로 판정합니다.
 * 분기/종료 상태(거절·반려·보류)는 happy-path 인덱스를 {@code -1} 로 두고 현재 라벨만 별도로 노출합니다.
 */
public final class CustomerOrderStepMapper {

    private CustomerOrderStepMapper() {
    }

    // 분기/종료 상태 표시용 인덱스 (happy-path 위에 있지 않음)
    private static final int BRANCH_INDEX = -1;

    private static final List<String> NORMAL_STEPS =
        List.of("결제완료", "상품제작중", "상품발송", "배송완료", "구매확정");

    private static final List<String> RETURN_STEPS =
        List.of("반품신청", "반품승인", "상품회수중", "상품확인중", "반품완료");

    private static final List<String> EXCHANGE_STEPS =
        List.of("교환신청", "교환승인", "상품회수중", "상품확인중", "교환진행", "상품발송", "배송완료", "교환완료");

    private static final List<String> CANCEL_STEPS =
        List.of("취소요청", "취소완료");

    public static CustomerOrderProgress resolve(OrderStatus orderStatus, OrderDeliveryStatus deliveryStatus) {
        CustomerOrderCategory category = CustomerOrderCategory.from(orderStatus);
        boolean delivered = deliveryStatus == OrderDeliveryStatus.DELIVERED;

        return switch (category) {
            case NORMAL -> normalProgress(orderStatus, delivered);
            case RETURN -> returnProgress(orderStatus);
            case EXCHANGE -> exchangeProgress(orderStatus, delivered);
            case CANCEL -> cancelProgress(orderStatus);
        };
    }

    private static CustomerOrderProgress normalProgress(OrderStatus status, boolean delivered) {
        int index;
        String label;
        switch (status) {
            case PAYMENT_COMPLETED -> {
                index = 0;
                label = "결제완료";
            }
            // 발주확인(ORDER_CONFIRMED)은 판매자 내부 단계이므로 소비자에게는 "상품제작중"으로 노출
            case ORDER_CONFIRMED, IN_PRODUCTION -> {
                index = 1;
                label = "상품제작중";
            }
            case SHIPPED -> {
                index = delivered ? 3 : 2;
                label = delivered ? "배송완료" : "상품발송";
            }
            case PURCHASE_CONFIRMED -> {
                index = 4;
                label = "구매확정";
            }
            default -> {
                index = 0;
                label = NORMAL_STEPS.get(0);
            }
        }
        return CustomerOrderProgress.of(CustomerOrderCategory.NORMAL, label, index, NORMAL_STEPS);
    }

    private static CustomerOrderProgress returnProgress(OrderStatus status) {
        int index = switch (status) {
            case RETURN_REQUESTED -> 0;
            case RETURN_APPROVED -> 1;
            case RETURN_PICKUP_IN_PROGRESS -> 2;
            case RETURN_INSPECTION, RETURN_PROCESSING -> 3;
            case RETURN_COMPLETED -> 4;
            // 반품거절/반품반려/반품보류 → 분기 (happy-path 밖)
            default -> BRANCH_INDEX;
        };
        return CustomerOrderProgress.of(
            CustomerOrderCategory.RETURN, status.getDescription(), index, RETURN_STEPS);
    }

    private static CustomerOrderProgress exchangeProgress(OrderStatus status, boolean delivered) {
        int index;
        String label = status.getDescription();
        switch (status) {
            case EXCHANGE_REQUEST -> index = 0;
            case EXCHANGE_APPROVED -> index = 1;
            case EXCHANGE_ITEM_COLLECTED -> index = 2;
            case EXCHANGE_ITEM_INSPECTING -> index = 3;
            case EXCHANGE_IN_PROGRESS -> index = 4;
            case EXCHANGE_ITEM_SHIPPED -> {
                index = delivered ? 6 : 5;
                label = delivered ? "배송완료" : "상품발송";
            }
            case EXCHANGE_COMPLETED -> index = 7;
            // 교환거절/교환반려/교환보류 → 분기 (happy-path 밖)
            default -> index = BRANCH_INDEX;
        }
        return CustomerOrderProgress.of(CustomerOrderCategory.EXCHANGE, label, index, EXCHANGE_STEPS);
    }

    private static CustomerOrderProgress cancelProgress(OrderStatus status) {
        int index = switch (status) {
            case CANCEL_REQUESTED -> 0;
            case CANCEL_APPROVED -> 1;
            // 취소반려 → 분기
            default -> BRANCH_INDEX;
        };
        return CustomerOrderProgress.of(
            CustomerOrderCategory.CANCEL, status.getDescription(), index, CANCEL_STEPS);
    }
}
