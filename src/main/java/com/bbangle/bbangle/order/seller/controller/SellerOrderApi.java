package com.bbangle.bbangle.order.seller.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.seller.controller.dto.request.OrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderDetailResponse.OrderDetail;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Seller Order", description = "(판매자) 주문 API")
public interface SellerOrderApi {

    @Operation(summary = "(판매자) 완료주문내역 페이징 조회")
    SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @ParameterObject Pageable pageable,
        @ParameterObject CompletedOrderFilter filter,
        Long sellerId
    );

    @Operation(summary = "(판매자) 주문 상세 조회")
    ListResult<OrderDetail> getCompletedOrders(
        List<Long> orderItemIds,
        Long sellerId
    );

    // TODO: v3
    @Operation(
        summary = "주문 품목 상세 정보 조회",
        description = "List에 주문 품목의 Id값을 받아 이를 상세 조회합니다."
    )
    ListResult<OrderItemDetailResponse> searchDetailItems(
        List<Long> orderItemList);


    @Operation(
        summary = "(판매자) 주문 내역 확인",
        description = "페이징 처리된 판매자의 전체 주문 품목을 조회합니다."
    )
    SingleResult<BbanglePageResponse<OrderSearchResponse>> searchOrders(
        @Parameter(description = "판매자 ID")
        Long sellerId,
        @Valid OrderRequest.OrderSearchRequest request,
        Pageable pageable);

    @Operation(summary = "(판매자) 발주 확인")
    SingleResult<OrderConfirmResponse> confirmOrder(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.OrderConfirmRequest request
    );
}
