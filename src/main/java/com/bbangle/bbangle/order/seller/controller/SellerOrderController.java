package com.bbangle.bbangle.order.seller.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.order.seller.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.seller.controller.dto.request.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchPageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ExchangeCreateResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ReturnCreateResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentModifyResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.claim.seller.service.SellerExchangeService;
import com.bbangle.bbangle.claim.seller.service.SellerReturnService;
import com.bbangle.bbangle.order.seller.service.SellerOrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/orders")
public class SellerOrderController implements SellerOrderApi {

    private final ResponseService responseService;

    private final SellerOrderService sellerOrderService;

    private final SellerReturnService sellerReturnService;

    private final SellerExchangeService sellerExchangeService;

    @Override
    @GetMapping("/completed")
    public SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        CompletedOrderFilter filter,
        @AuthenticationPrincipal Long sellerId) {
        // TODO: 구현 필요
        List<OrderSummary> orderSummaries = List.of(OrderSummary.sample());
        PageImpl<OrderSummary> page = new PageImpl<>(orderSummaries, pageable, 10);
        BbanglePageResponse<OrderSummary> response = BbanglePageResponse.of(page);
        return responseService.getSingleResult(response);
    }

    @Override
    @PostMapping("/items")
    public ListResult<OrderItemDetailResponse> searchDetailItems(
        @AuthenticationPrincipal Long sellerId,
        @RequestBody List<Long> orderItemList) {
        List<OrderItemDetailResponse> responses = sellerOrderService.searchOrderItemDetails(orderItemList, sellerId);
        return responseService.getListResult(responses);
    }

    @Override
    @PostMapping("/list")
    public SingleResult<OrderSearchPageResponse> searchOrders(
        @AuthenticationPrincipal Long sellerId,
        @RequestBody OrderSearchRequest request,
        @PageableDefault(size = 100, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        OrderSearchPageResponse response = sellerOrderService.orderSearch(
            request.toCommand(sellerId, pageable));

        return responseService.getSingleResult(response);
    }

    @PostMapping("/{orderId}/confirm")
    @Override
    public SingleResult<OrderConfirmResponse> confirmOrder(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.OrderConfirmRequest request
    ) {
        var result = sellerOrderService.confirmOrder(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

    @PostMapping("/{orderId}/shipment")
    public SingleResult<ShipmentRegisterResponse> registerShipment(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ShipmentRegisterRequest request
    ) {
        var result = sellerOrderService.registerShipment(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

    @PostMapping("/{orderId}/returns")
    @Override
    public SingleResult<ReturnCreateResponse> createReturn(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ReturnCreateRequest request
    ) {
        var result = sellerReturnService.createReturn(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

    @PostMapping("/{orderId}/exchanges")
    @Override
    public SingleResult<ExchangeCreateResponse> createExchange(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ExchangeCreateRequest request
    ) {
        var result = sellerExchangeService.createExchange(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

    @PatchMapping("/{orderId}/shipment")
    @Override
    public SingleResult<ShipmentModifyResponse> modifyShipment(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ShipmentModifyRequest request
    ) {
        var result = sellerOrderService.modifyShipment(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

}
