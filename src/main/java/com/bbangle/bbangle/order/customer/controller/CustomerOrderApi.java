package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Customer Order", description = "(소비자) 주문 API")
public interface CustomerOrderApi {

    @Operation(
        summary = "(소비자) 주문목록 조회",
        description = "인증된 회원의 주문을 주문일 최신순으로 페이징 조회합니다. "
            + "각 주문상품의 진행 단계(일반/반품/교환/취소)와 탭별 카운트(statusCounts)가 포함됩니다."
    )
    SingleResult<CustomerOrderPageResponse> getOrders(
        Long memberId,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "(소비자) 주문 상세 조회",
        description = "인증된 회원 본인 소유의 단일 주문 상세를 조회합니다. "
            + "결제금액(반품·취소 제외), 배송지, 주문상품별 진행 단계와 상태 뱃지/할인율/태그를 포함합니다."
    )
    SingleResult<CustomerOrderDetail> getOrderDetail(
        Long memberId,
        @Parameter(description = "주문 ID", example = "1") Long orderId
    );

    @Operation(
        summary = "(소비자) 구매확정",
        description = "본인 소유의 배송완료된 주문상품을 구매확정합니다. "
            + "배송완료 후 7일이 지나면 자동으로 구매확정되며, 그 전이라도 이 API로 직접 확정할 수 있습니다. "
            + "구매확정 이후에는 주문 조회 응답의 reviewable 플래그가 true 가 되어 후기작성 버튼이 노출됩니다."
    )
    SingleResult<Long> confirmPurchase(
        Long memberId,
        @Parameter(description = "주문 ID", example = "1") Long orderId,
        @Parameter(description = "주문상품 ID", example = "10") Long orderItemId
    );
}
