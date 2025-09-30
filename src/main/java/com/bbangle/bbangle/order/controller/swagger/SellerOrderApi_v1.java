package com.bbangle.bbangle.order.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Order", description = "(판매자) 주문 API")
public interface SellerOrderApi_v1 {

    @Operation(summary = "(판매자) 완료주문내역 페이징 조회")
    SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @ParameterObject Pageable pageable,
        @ParameterObject CompletedOrderFilter filter,
        Long memberId
    );
}
