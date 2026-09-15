package com.bbangle.bbangle.order.customer.controller.dto.response;

import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 소비자 주문 상세 조회 응답.
 *
 * <p>화면 구성(노출 항목)에 맞춰 4개 블록으로 구성됩니다.
 * <ul>
 *   <li>헤더: 주문일자/주문번호</li>
 *   <li>① 결제 금액({@link CustomerPaymentSummary}): 상품금액/할인/배송비/최종결제금액/영수증</li>
 *   <li>② 배송지({@link CustomerDeliveryInfo}): 배송지명/주소/연락처/요청사항/배송지변경 가능여부</li>
 *   <li>상품({@link CustomerOrderDetailItem}): 스토어/상품/상태뱃지/가격/태그/배송조회</li>
 * </ul>
 */
public class CustomerOrderDetailResponse {

    @Schema(description = "소비자 주문 상세 응답")
    public record CustomerOrderDetail(
        @Schema(description = "주문 ID", example = "1") Long orderId,
        @Schema(description = "주문번호", example = "ORDER-2025-05-13-00001") String orderNumber,
        @Schema(description = "주문일자", example = "2025-05-13")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate orderDate,
        @Schema(description = "① 결제 금액") CustomerPaymentSummary payment,
        @Schema(description = "② 배송지") CustomerDeliveryInfo delivery,
        @Schema(description = "주문 상품 목록") List<CustomerOrderDetailItem> orderItems
    ) {

    }

    /**
     * ① 결제 금액 블록.
     * 금액 집계는 정책에 따라 <b>반품·취소 상품 금액을 제외</b>하고 계산합니다.
     */
    @Schema(description = "결제 금액 정보 (반품·취소 금액 제외)")
    public record CustomerPaymentSummary(
        @Schema(description = "상품금액(정가 합계)", example = "26100") long productAmount,
        @Schema(description = "상품 할인금액", example = "1700") long productDiscountAmount,
        @Schema(description = "배송비", example = "0") long deliveryFee,
        @Schema(description = "최종 결제금액", example = "24400") long finalPaymentAmount,
        @Schema(description = "총 할인 합계 금액", example = "1700") long totalDiscountAmount,
        @Schema(description = "총 할인 합계 문구", example = "총 1,700원 할인 받았어요") String totalDiscountMessage,
        @Schema(description = "영수증 보기 노출 여부 (반품·취소 제외 결제건 존재 시 true)") boolean receiptViewable,
        @Schema(description = "결제 정보(상태/수단/결제일)") PaymentInfo paymentInfo
    ) {

    }

    /**
     * ② 배송지 블록.
     * 주문 내 배송 정보는 일괄 처리되므로 대표 배송정보 1건으로 노출합니다.
     */
    @Schema(description = "배송지 정보")
    public record CustomerDeliveryInfo(
        @Schema(description = "배송지명(수령인)", example = "홍길동") String recipientName,
        @Schema(description = "주소(기본+상세)", example = "서울특별시 강남구 테헤란로 123 101동 1001호") String address,
        @Schema(description = "우편번호", example = "06234") String zipCode,
        @Schema(description = "연락처", example = "010-1234-5678") String phone,
        @Schema(description = "배송 요청사항", example = "부재 시 경비실에 맡겨주세요") String deliveryMemo,
        @Schema(description = "배송지 변경 가능 여부 (결제완료 상품 존재 시 true)") boolean addressChangeable
    ) {

    }

    @Schema(description = "주문 상품 정보 + 진행 단계")
    public record CustomerOrderDetailItem(
        @Schema(description = "주문상품 ID", example = "10") Long orderItemId,
        @Schema(description = "스토어명", example = "비건빵빵이네") String storeName,
        @Schema(description = "상품명(게시글명)", example = "저당 베이글 세트") String boardTitle,
        @Schema(description = "옵션(상품명)", example = "저칼로리 베이글") String productName,
        @Schema(description = "상태 뱃지", example = "상품발송") String statusBadge,
        @Schema(description = "주문 상태(원천 enum)") OrderStatus orderStatus,
        @Schema(description = "정가(단가)", example = "5800") long productPrice,
        @Schema(description = "판매가(할인 단가)", example = "4700") long unitPrice,
        @Schema(description = "할인율(%)", example = "19") int discountRate,
        @Schema(description = "수량", example = "2") Integer quantity,
        @Schema(description = "상품 총액(판매가×수량)", example = "9400") long totalPrice,
        @Schema(description = "상품 태그", example = "[\"고단백\", \"글루텐프리\"]") List<String> tags,
        @Schema(description = "배송조회 버튼 노출 여부 (운송장 등록 시 true)") boolean deliveryTrackable,
        @Schema(description = "택배사", example = "CJ대한통운") String courierCompany,
        @Schema(description = "운송장 번호", example = "123-456-789") String trackingNumber,
        @Schema(description = "진행 단계 정보") CustomerOrderProgress progress,
        @Schema(description = "구매확정 버튼 노출 여부 (배송완료 && 구매확정 전이면 true)") boolean purchaseConfirmable,
        @Schema(description = "후기작성 버튼 노출 여부 (구매확정 상태면 true)") boolean reviewable
    ) {

    }
}
