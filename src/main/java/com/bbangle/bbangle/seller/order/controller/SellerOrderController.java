package com.bbangle.bbangle.seller.order.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.seller.order.controller.dto.request.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.seller.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderDetailResponse.OrderDetail;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.seller.order.controller.swagger.SellerOrderApi;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController implements SellerOrderApi {

    private final ResponseService responseService;

    @Override
    @GetMapping("/completed")
    public SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable,
        CompletedOrderFilter filter,
        @AuthenticationPrincipal
        Long memberId
    ) {
        // TODO: 구현 필요
        List<OrderSummary> orderSummaries = List.of(OrderSummary.sample());
        PageImpl<OrderSummary> page = new PageImpl<>(orderSummaries, pageable, 10);
        BbanglePageResponse<OrderSummary> response = BbanglePageResponse.of(page);
        return responseService.getSingleResult(response);
    }

    @Override
    @GetMapping
    public ListResult<OrderDetail> getCompletedOrders(
        @RequestParam List<Long> orderItemIds,
        @AuthenticationPrincipal Long memberId
    ) {
        // TODO: 구현 필요
        OrderDetail orderDetail = OrderDetail.sample();
        List<OrderDetail> orderDetails = List.of(orderDetail);
        return responseService.getListResult(orderDetails);
    }

    @Override
    @PostMapping("/orders/items")
    public ListResult<OrderItemDetailResponse> searchDetailItems(
        @RequestBody
        List<Long> orderItemList) {

        List<OrderItemDetailResponse> responses = orderItemList.stream()
            .map(id -> new OrderItemDetailResponse(
                // 주문번호
                "ORDER-2025-04-05-test",

                // 주문 정보
                new OrderItemDetailResponse.OrderInfo(
                    "2025-04-05",                // orderDate (String, yyyy-MM-dd)
                    "반품-상품발송"                 // orderStatusLabel
                ),

                // 주문자 정보
                new OrderItemDetailResponse.BuyerInfo(
                    "홍길동",                     // recipientName
                    "홍길동",                     // buyerName
                    "010-1234-5678",             // buyerPhone1
                    "010-9876-5432"              // buyerPhone2
                ),

                // 배송 정보
                new OrderItemDetailResponse.ShippingInfo(
                    "수거중",                     // statusLabel
                    "CJ대한통운",                 // courierCompany
                    "1234-5678-910",             // trackingNumber
                    3000L,                       // shippingFee
                    "서울시 강남구 예제로 123",    // address
                    "문 앞에 두세요."              // memo
                ),

                // 주문 상품
                new OrderItemDetailResponse.OrderItem(
                    "예제 상품",                  // boardTitle
                    "예제 상품",                  // itemName
                    2,                           // quantity
                    50_000L,                     // unitPrice
                    100_000L                     // totalPrice
                )
            ))
            .toList();
        return responseService.getListResult(responses);
    }

    @Override
    @GetMapping("/{sellerId}/orders")
    public PaginatedResponse<OrderSearchResponse> searchOrders(
        @PathVariable(name = "sellerId")
        Long sellerId,
        @ModelAttribute OrderSearchRequest request) {

        // Mock 데이터 생성
        List<OrderSearchResponse> mockOrders = List.of(
            new OrderSearchResponse(
                "PAID",
                "PREPARING_PRODUCT",
                "CJ 대한통운",
                "1234567890",
                "ORDER-20240921-00001",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                "홍길동",
                "맛있는 식빵"
            ),
            new OrderSearchResponse(
                "PAID",
                "SHIPPING",
                "우체국 택배",
                "0987654321",
                "ORDER-20240921-00002",
                LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_DATE_TIME),
                "김철수",
                "달콤한 소금빵"
            )
        );

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        // 전체 아이템 개수는 480개로 가정
        Page<OrderSearchResponse> resultPage = new PageImpl<>(mockOrders, pageable, 480);

        PaginatedResponse<OrderSearchResponse> response = new PaginatedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPageNumber(resultPage.getNumber());
        response.setPageSize(resultPage.getSize());
        response.setTotalPages(resultPage.getTotalPages());
        response.setTotalElements(resultPage.getTotalElements());

        return responseService.getPagingResult(response);
    }

}
