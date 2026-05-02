package com.bbangle.bbangle.order.seller.service.model;

import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSearchCondition;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

/**
 * 판매자 주문 관련 커맨드(Command) 객체 모음.
 * Controller에서 Service 계층으로 요청 정보를 전달하는 불변 값 객체(record)입니다.
 */
public class SellerOrderCommand {

    /**
     * 발주 확인 처리 커맨드.
     * 판매자가 특정 주문의 OrderItem 목록에 대해 발주를 확정할 때 사용합니다.
     * (OrderItem 상태: 결제완료 → 발주확인)
     */
    @Builder
    public record OrderConfirmCommand(
        Long orderId,
        List<Long> orderItemIds,
        Long sellerId
    ) {
    }

    /**
     * 신규 운송장 등록 커맨드.
     * 발주 확인된 OrderItem에 택배사·운송장 번호를 처음 등록할 때 사용합니다.
     * OrderDelivery가 없으면 새로 생성하고, 있으면 기존 것에 운송장 정보를 덮어씁니다.
     */
    @Builder
    public record ShipmentRegisterCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
    ) {
    }

    /**
     * 기존 운송장 수정 커맨드.
     * 이미 등록된 운송장 정보(택배사·운송장 번호)를 변경할 때 사용합니다.
     * OrderDelivery가 반드시 존재해야 합니다 (없으면 DELIVERY_NOT_FOUND 예외로 실패 처리됨).
     */
    @Builder
    public record ShipmentModifyCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
     ) {
    }

    /**
     * 주문 목록 검색 커맨드 (배송 전 주문 탭 기준).
     * 판매자 주문 관리 화면에서 결제완료~배송완료 상태의 주문을 조회할 때 사용합니다.
     */
    @Builder
    public record OrderSearchCommand(
        Long sellerId,
        OrderDeliveryStatus orderDeliveryStatus,
        CompletedOrderSearchType searchType,
        DefaultSearchCondition searchCondition,
        Pageable page
    ) {
    }

    /**
     * 완료 주문 목록 검색 커맨드 (구매확정/취소/반품/교환 탭 기준).
     * 결제가 종결된 주문(구매확정, 취소, 반품, 교환)을 날짜·상태·키워드로 조회할 때 사용합니다.
     */
    @Builder
    public record CompletedOrderSearchCommand(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate,
        CompletedOrderStatus status,
        CompletedOrderSearchType searchType,
        String searchValue,
        Pageable pageable
    ) {
    }

}
