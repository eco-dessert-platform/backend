package com.bbangle.bbangle.order.seller.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.seller.controller.dto.request.OrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchPageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentModifyResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
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

    @Operation(summary = "주문 품목 상세 정보 조회")
    ListResult<OrderItemDetailResponse> searchDetailItems(
        Long sellerId,
        List<Long> orderItemList);


    @Operation(
        summary = "(판매자) 주문 내역 확인",
        description = "페이징 처리된 판매자의 전체 주문 품목을 조회합니다. 응답에 상태별 카운트(statusCounts)가 포함됩니다."
    )
    SingleResult<OrderSearchPageResponse> searchOrders(
        @Parameter(description = "판매자 ID")
        Long sellerId,
        @Valid OrderRequest.OrderSearchRequest request,
        @ParameterObject Pageable pageable);

    @Operation(summary = "(판매자) 발주 확인")
    SingleResult<OrderConfirmResponse> confirmOrder(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.OrderConfirmRequest request
    );

    @Operation(summary = "(판매자) 운송장 입력")
    SingleResult<ShipmentRegisterResponse> registerShipment(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ShipmentRegisterRequest request
    );

    @Operation(summary = "(판매자) 운송장 수정")
    SingleResult<ShipmentModifyResponse> modifyShipment(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ShipmentModifyRequest request
    );
}
