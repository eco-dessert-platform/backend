package com.bbangle.bbangle.seller.store.order.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.order.controller.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.seller.store.order.controller.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.seller.store.order.controller.OrderResponse_v3.OrderItemDetailResponse;
import com.bbangle.bbangle.seller.store.order.controller.swagger.OrderApi_v3;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller")
public class OrderController_v3 implements OrderApi_v3 {

    private final ResponseService responseService;

    @Override
    @PostMapping("/orders/items")
    public ListResult<OrderItemDetailResponse> searchDetailItems(
        @RequestBody
        List<Long> orderItemList) {

        List<OrderResponse_v3.OrderItemDetailResponse> responses = orderItemList.stream()
            .map(id -> new OrderResponse_v3.OrderItemDetailResponse(
                // 주문번호
                "ORDER-2025-04-05-test",

                // 주문 정보
                new OrderResponse_v3.OrderItemDetailResponse.OrderInfo(
                    "2025-04-05",                // orderDate (String, yyyy-MM-dd)
                    "반품-상품발송"                 // orderStatusLabel
                ),

                // 주문자 정보
                new OrderResponse_v3.OrderItemDetailResponse.BuyerInfo(
                    "홍길동",                     // recipientName
                    "홍길동",                     // buyerName
                    "010-1234-5678",             // buyerPhone1
                    "010-9876-5432"              // buyerPhone2
                ),

                // 배송 정보
                new OrderResponse_v3.OrderItemDetailResponse.ShippingInfo(
                    "수거중",                     // statusLabel
                    "CJ대한통운",                 // courierCompany
                    "1234-5678-910",             // trackingNumber
                    3000L,                       // shippingFee
                    "서울시 강남구 예제로 123",    // address
                    "문 앞에 두세요."              // memo
                ),

                // 주문 상품
                new OrderResponse_v3.OrderItemDetailResponse.OrderItem(
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
