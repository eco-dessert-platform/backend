package com.bbangle.bbangle.order.seller.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.order.seller.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.seller.controller.dto.request.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.CompletedOrderPageResponse;
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

    /**
     * 완료 주문 목록 조회 (구매확정·취소·반품·교환 상태)
     * 기본 페이지 크기 10: 완료 주문은 실시간 처리 대상이 아니므로 소량 조회합니다.
     */
    @Override
    @GetMapping("/completed")
    public SingleResult<CompletedOrderPageResponse> getCompletedOrders(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        CompletedOrderFilter filter,
        @AuthenticationPrincipal Long sellerId) {

        CompletedOrderPageResponse response = sellerOrderService.getCompletedOrders(
            filter.toCommand(sellerId, pageable));

        return responseService.getSingleResult(response);
    }

    /**
     * 주문 항목 상세 조회
     * 선택된 orderItemId 목록에 대한 구매자·배송·상품 정보를 반환합니다.
     */
    @Override
    @PostMapping("/items")
    public ListResult<OrderItemDetailResponse> searchDetailItems(
        @AuthenticationPrincipal Long sellerId,
        @RequestBody List<Long> orderItemList) {
        List<OrderItemDetailResponse> responses = sellerOrderService.searchOrderItemDetails(orderItemList, sellerId);
        return responseService.getListResult(responses);
    }

    /**
     * 주문 목록 검색 (결제완료~배송완료 상태)
     * @PageableDefault(size = 100): 판매자가 주문을 한 번에 내려받아 일괄 처리하는 워크플로우를
     * 지원하기 위해 기본 페이지 크기를 100으로 설정합니다.
     */
    @Override
    @PostMapping("/list")
    public SingleResult<OrderSearchPageResponse> searchOrders(
        @AuthenticationPrincipal Long sellerId,
        @Valid @RequestBody OrderSearchRequest request,
        @PageableDefault(size = 100, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        OrderSearchPageResponse response = sellerOrderService.orderSearch(
            request.toCommand(sellerId, pageable));

        return responseService.getSingleResult(response);
    }

    /**
     * 발주 확인 처리: 결제완료 상태 → 발주확인 상태로 전환
     * 부분 성공 정책: 일부 항목이 실패해도 가능한 항목의 처리 결과를 반환합니다.
     */
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

    /**
     * 신규 운송장 등록: 발주확인 상태 → 배송중 상태로 전환
     * OrderDelivery가 없으면 새로 생성하여 택배사·운송장 번호를 설정합니다.
     */
    @PostMapping("/{orderId}/shipment")
    public SingleResult<ShipmentRegisterResponse> registerShipment(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable Long orderId,
        @Valid @RequestBody SellerOrderRequest.ShipmentRegisterRequest request
    ) {
        var result = sellerOrderService.registerShipment(request.toCommand(sellerId, orderId));
        return responseService.getSingleResult(result);
    }

    /**
     * 반품 접수 처리: 고객 반품 요청 항목에 대해 판매자가 반품 수거를 시작합니다.
     */
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

    /**
     * 교환 접수 처리: 고객 교환 요청 항목에 대해 판매자가 교환 처리를 시작합니다.
     */
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

    /**
     * 운송장 수정: 이미 등록된 운송장 정보(택배사·운송장 번호)를 변경합니다.
     * 기존 OrderDelivery가 존재해야 수정 가능하며, 없으면 실패 응답을 반환합니다.
     */
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
