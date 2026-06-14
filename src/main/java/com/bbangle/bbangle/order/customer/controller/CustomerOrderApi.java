package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import io.swagger.v3.oas.annotations.Operation;
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
}
