package com.bbangle.bbangle.order.customer.controller.dto.response;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 소비자 주문목록 조회 응답 모음.
 */
public class CustomerOrderResponse {

    @Schema(description = "소비자 주문목록 조회 응답 (페이지 + 탭 카운트)")
    public record CustomerOrderPageResponse(
        @Schema(description = "주문 목록 페이지")
        BbanglePageResponse<CustomerOrderInfo> orders,

        @Schema(description = "탭별 카운트")
        CustomerOrderStatusCounts statusCounts
    ) {

    }

    @Schema(description = "소비자 주문 탭별 카운트")
    public record CustomerOrderStatusCounts(
        @Schema(description = "전체", example = "27") long total,
        @Schema(description = "진행중 (결제완료~배송완료, 구매확정 전)", example = "12") long inProgress,
        @Schema(description = "구매확정", example = "10") long purchased,
        @Schema(description = "취소", example = "2") long canceled,
        @Schema(description = "반품", example = "2") long returned,
        @Schema(description = "교환", example = "1") long exchanged
    ) {

        public static CustomerOrderStatusCounts of(
            long inProgress,
            long purchased,
            long canceled,
            long returned,
            long exchanged
        ) {
            long total = inProgress + purchased + canceled + returned + exchanged;
            return new CustomerOrderStatusCounts(total, inProgress, purchased, canceled, returned, exchanged);
        }
    }

    @Schema(description = "주문 단위 정보 (날짜별 그룹핑 키 orderDate 포함)")
    public record CustomerOrderInfo(
        @Schema(description = "주문 ID") Long orderId,
        @Schema(description = "주문번호") String orderNumber,
        @Schema(description = "주문일", example = "2025-06-14")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate orderDate,
        @Schema(description = "총 주문금액") Long totalAmount,
        @Schema(description = "결제 정보") PaymentInfo paymentInfo,
        @Schema(description = "주문 상품 목록") List<CustomerOrderItemInfo> orderItems
    ) {

    }

    @Schema(description = "주문 상품 정보 + 진행 단계")
    public record CustomerOrderItemInfo(
        @Schema(description = "주문상품 ID") Long orderItemId,
        @Schema(description = "게시글명") String boardTitle,
        @Schema(description = "상품명") String productName,
        @Schema(description = "수량") Integer quantity,
        @Schema(description = "단가") Long unitPrice,
        @Schema(description = "상품 총액") Long totalPrice,
        @Schema(description = "주문 상태(원천 enum)") OrderStatus orderStatus,
        @Schema(description = "배송 상태") String deliveryStatus,
        @Schema(description = "택배사") String courierCompany,
        @Schema(description = "운송장 번호") String trackingNumber,
        @Schema(description = "진행 단계 정보") CustomerOrderProgress progress
    ) {

    }

    @Schema(description = "소비자 화면 진행 단계 정보")
    public record CustomerOrderProgress(
        @Schema(description = "주문 유형") CustomerOrderCategory category,
        @Schema(description = "현재 단계 라벨", example = "배송완료") String currentStep,
        @Schema(description = "현재 단계 인덱스 (0부터, 분기/종료 상태는 -1)", example = "3") int currentStepIndex,
        @Schema(description = "해당 유형의 전체 진행 단계 라벨") List<String> steps
    ) {

        public static CustomerOrderProgress of(
            CustomerOrderCategory category,
            String currentStep,
            int currentStepIndex,
            List<String> steps
        ) {
            return new CustomerOrderProgress(category, currentStep, currentStepIndex, steps);
        }
    }
}
